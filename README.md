# NetChess
Own-developed chess game for the android platform. It provides you 3 types of game: 

1. One-device game. It has multiple settings, where you can chose oldX specific time-control for each player.
2. Lan game. This type of game is based on implimentation of peer-2-peer connection via WiFi-direct function.
3. Online game. Rated games for registered player. Rank-list with all users and rated games with simple list of moves are presented.

Advantages:
- different time-control options for both players;
- personal user profile with players photo, info about wins, draws, losses, and elo-rating;
- pause and mini-chat features til the game is going are also in;
- it's working :)
 
 Drawbacks:
  - Known bugs:
         - Application can crash, if activity is dead. I'm going to fix it when as soon as I have more time.
  - Code imperfection:
         - Code is cluttered and UI logic depends on buisness logic and in reverse.
         - Bloated functions.
         - Fragments and activity interaction is not the best. (interfaces and RX libraries as the best choice, so as instead of AsyncTasks).
        
Possible improvements as I see my app now:
        - Refactor code according to MVP pattern.
        - Shrink functions\methods size deviding them.
        - Include libraries such as Dagger, RxJava, RxAndroid, Retrolambda, AndroidAnnotations etc. 
        - Other idieas would be on the surface after the previous steps have being fullfilled.
        
Please, feel free to contact me if you have any ideas how to improve the code and, of course, if any bugs been discovered. I realy appreciate any helpful remarks. Thank you. 
