This contains the basic code for creating a media notificaiton that can play, stop, and skip.

- Manger.kt creates the notification and its actions
- MediaPlaybackService.kt creates a MediaSession and creates a callback from MySessionCallback.kt
- MySessionCallback.kt implements play, skip, and previous functions just with log messages
