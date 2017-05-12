# Icarus Air Traffic Control
Icarus ATC is an air traffic control simulator game. The main focus is on managing the movement of arrivals, flyovers, and departures to prevent disaster while maintaining efficiency.

## Airplane Control
Airplanes will appear at the edges of the map or on the runway at random intervals. There are three classes of flights: arrivals, flyovers, and departures. 

### Flyovers
Flyovers cruise at 10,000m and need only to be pointed in the direction of their destination and be handed off. They fly faster than other airplanes, but can still change altitude and target a specific heading or waypoint.

### Arrivals
These airplanes appear on the edges of the map at 2000m altitude. The player must guide them to a successful landing, though there is a rudimentary autopilot that will take care of the final approach and landing phases. Be sure to start the landing from far away, because the airplane cannot land it it's too close to the runway.

### Departures
Departures start in a "queue." Click on the airport icon to clear the next plane for departure, and then select the end of the runway it should start on. Once on the runway, the airplane will automatically accelerate and take off, but must be handed off to its destination before it leaves the map.

## Points
The player earns points for successful landings and handoffs, but loses points if a plane leaves the map without being handed off or if two planes fly too close to each other. A crash is hard to get but catastrophic in terms of points.
