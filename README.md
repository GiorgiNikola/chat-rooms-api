# Chat Rooms API

Spring Boot REST + WebSocket API for real-time group messaging with per-room role-based access control. Personal project, built to learn WebSocket/STOMP on top of a REST foundation similar to Task Tracker API.

## Stack

Java 17, Spring Boot 4.0, Spring Security (JWT), Spring WebSocket/STOMP over SockJS, Spring Data JPA, H2, MapStruct, Lombok, SpringDoc OpenAPI.

## Getting it running

Clone it, then run with the wrapper:

```bash
./mvnw spring-boot:run
```

Runs on `http://localhost:8080`. H2 is in-memory, no separate DB setup, resets on every restart.

Set `JWT_SECRET` and `EXPIRATION_TIME` as environment variables before starting, there are no defaults baked in.

## Frontend

A minimal static test harness lives in `src/main/resources/static` (`index.html`, `app.js`, `style.css`) — no framework, no build step. It exists to demonstrate the realtime messaging live across two browser tabs, not as a polished client. Login, room list, and a chat pane that connects over WebSocket once you open a room. Visit `http://localhost:8080/` after starting the app.

## Auth

Register, login, get a JWT, send `Authorization: Bearer <token>` on REST calls. Stateless, no sessions. WebSocket connections authenticate once at the STOMP `CONNECT` frame (same JWT, attached as a STOMP header instead of an HTTP header), since a persistent connection doesn't get a header replayed per message the way REST does.

## Roles

Roles are per-room, not global, tracked on a `RoomMembership` join entity rather than on the user: **OWNER** (created the room, full control, ownership is transferable), **MODERATOR** (kick/delete any message in the room), **MEMBER** (send messages, leave, edit/delete own messages). A user can be OWNER of one room and a plain MEMBER of another. There's no global ADMIN role in this version, deliberately, to keep the focus on the per-room model.

## Endpoints

```
POST   /api/auth/register                              public
POST   /api/auth/login                                 public

POST   /api/rooms                                       any authenticated user (creator becomes OWNER)
GET    /api/rooms/{id}                                  any authenticated user
GET    /api/rooms                                        any authenticated user, paginated
PUT    /api/rooms/{id}                                  any authenticated user
DELETE /api/rooms/{id}                                  any authenticated user

POST   /api/rooms/{roomId}/members                       any authenticated user (join)
DELETE /api/rooms/{roomId}/members/me                     any member (leave, blocked for OWNER)
PATCH  /api/rooms/{roomId}/members/{userId}/promote       OWNER only
PATCH  /api/rooms/{roomId}/members/{userId}/demote        OWNER only
DELETE /api/rooms/{roomId}/members/{userId}                OWNER/MODERATOR (kick, blocked against OWNER)

POST   /api/rooms/{roomId}/messages                      room member
GET    /api/rooms/{roomId}/messages                      room member, paginated + date-range filterable
PATCH  /api/rooms/{roomId}/messages/{messageId}            message sender only
DELETE /api/rooms/{roomId}/messages/{messageId}            sender or room OWNER/MODERATOR (soft delete)
```

## WebSocket

STOMP over SockJS at `/ws`. Clients subscribe to `/topic/rooms/{roomId}` to receive live messages, and send to `/app/rooms/{roomId}/send` to post one. Both directions are membership-checked: subscribing to or sending in a room you're not a member of is rejected at the STOMP frame level, not just on the REST side. Sending over WebSocket persists the message through the same service the REST endpoint uses, so both paths behave identically, WebSocket is the live path, REST is what you'd use for history or a client that can't hold a socket open.

## Testing

Manual testing throughout development: individual REST requests via Postman, and a small standalone HTML/STOMP.js test page for exercising the WebSocket layer. No exported Postman collection, no Swagger/OpenAPI documentation set up beyond the dependency being present, and no JUnit test suite yet.

## Known limitations

Room membership/authorization goes through a manual per-method check in the service layer for actions like edit/delete message (sender-or-moderator), since the check depends on data fetched inside the method (who sent this specific message) rather than being derivable from the request parameters alone. Room-level actions like promote/demote/kick use `@PreAuthorize` with a custom `RoomSecurity` bean instead, since those checks are static given just the room ID and the caller. Both approaches are used deliberately, not inconsistently.

JWT staleness: since H2 is in-memory and wipes on restart, a token issued before a restart still passes signature/expiry validation after the restart even though the user it names no longer exists in the (now empty) database. Not an issue with a persistent database in a real deployment.

No global ADMIN role. Deferred to keep focus on per-room RBAC; could be added as a `platformRole` field on `User`, orthogonal to room-scoped roles.

Room ownership is designed to be transferable (no `updatable = false` on the owner field), but no transfer endpoint exists yet, ownership is currently fixed after room creation in practice.

All rooms are joinable directly regardless of the `isPrivate` flag. The flag exists on the entity per the original spec but there's no separate request-to-join flow for private rooms yet, that's the next real gap to close if this project continues past its current scope.

Presence tracking (online member list per room) and stretch features (typing indicators, read receipts) were scoped out.