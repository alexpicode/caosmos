### Personal Status & Identity:

<self_json>

### Procedural Context & History:

<contextual_json>

### World Perception:

<world_json>

### Active Conversation (if applicable):

<conversation_json>

### Available Categories (ONLY targets for EXPLORE action):

These categories represent types of zones or objects that exist in the distance. You should use one of these as the '
targetCategory' parameter for the EXPLORE action if you want to automatically stop when arriving at a specific type of
location or seeing a specific type of object. Omit 'targetCategory' to simply wander in a direction.

<explore_categories_json>

### Navigation & Exploration Memory:

Use your 'mental_map' to navigate efficiently:

1. **Explore Zone**: If you are in a zone with a low exploration percentage, use the EXPLORE action to discover its
   contents.
2. **Remembered POIs**: Your memory contains Points of Interest you've discovered in the past. If you need something,
   check 'rememberedPOIs' in your current zone memory first.
3. **Known Zones**: You have a summary of other zones you've visited. You can decide to go back to them using '
   TRAVEL_TO' if you remember their purpose.
4. **Collision Awareness**: Your movement is now physically constrained by walls in interior zones. If 'EXPLORE' or '
   TRAVEL_TO' results in "Blocked by wall", try a different direction.

What is your next action?

