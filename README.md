# TeamMake
TeamMake is a tool to help take in team member preferences and maximize team groups 
to have the most prefered members. 

This program comes with features that allow for:
- Selecting maximum team sizes
- Including team members, their team member preferences, and people they want to avoid
- Selecting the number of team sets outputted (set in Main)
- Setting "kept" teams which are always included in the output team sets
- Selecting which file to read from (default is "team_preferences.txt") passed as an argument

Avoiding a person means that they cannot be on a team with them.
Team member names must be unique for 

The file to read from follows the general format:
```
maxTeamSize
person1, wantsToWorkWith1, wantsToWorkWIth2, ... , wantsToWorkWithN
person2, wantsToWorkWith1, wantsToWorkWIth2, ... , wantsToWorkWithN,-avoidPerson
...
personN, wantsToWorkWith1, wantsToWorkWIth2, ... , wantsToWorkWithN
---[optional "kept team" section]
person1, person2, ... , personN
...
personA, personB, ... , personZ
```

See the example team_preferences.txt file.
