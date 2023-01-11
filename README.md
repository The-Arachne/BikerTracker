# Biker GPS Tracker - simple map with routes/pins/geofence...
Funny 2 week long projet to pass study subject without exam.

required features implemented:
- **Firebase AUTH:** simple email/passwd authentication without creating account
- **ROOM DB:** saving and reading app state and data
- **Firebase RealtimeDatabase:** syncing users account -> custom sync room with firebase DB
- **Map:** used OSMDroid OpenStreetMap with routes and pins
- **Service:** background service to retreive user position and append it to route
- **Geofence:** show notification when user enters location previously created in route
- **Broadcast Receiver:** listenig to geofence triggers and then shows notification in status bar
- **List:** list of saved to account routes
- **Simple UI** no need to explain
