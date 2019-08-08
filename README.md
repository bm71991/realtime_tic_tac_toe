# realtime_tic_tac_toe

An Android Application written in Kotlin which employs an MVVM architecture and incorporates libraries from Android   Architecture Components. Registered users either wait for the next available player or get matched with a queued opponent. This project was done completely on my own, the aim being to learn more about Android development.

# User Access
``user_access`` handles registration and authentication. When a user sucessfully signs up for an account, a document is added to the
collection "users". 

```
users {
  <username>  {
    email: <email>,
    uid: <uid>
  },
  ...
}
```
This collection serves two purposes:
1. When signing up for an account, verifies that the username is not taken. If the selected username is already the key of a document, the sign-up will not be successful. 
2. The Firebase SDK provides email but not username sign-in functionality. To circumvent this, the application uses the entered username to query Firestore for the appropriate email. This will then be used in conjunction with the entered password in order to authenticate with Firebase.

Once the required information is entered into ``LoginFragment`` or ``SignupFragment``, a method in ``UserAccessViewModel`` will be called to query Firestore/Firebase. The ViewModel calls ``UserAccessRepository`` methods and handles the ``Task`` objects that they return, propagating the success or failure of these calls up to the Fragment level.  

# Finding a New Game / Game Creation

The ``games`` collection contains a single ``waitingPlayers`` document and a variable amount of documents which represent games in progress. The structure of ``waitingPlayers`` is as follows:

```
waitingPlayers {
  playerPairs: [
    0 {
      id: <uid>,
      player1: <player 1 username>,
      player2: <player 2 username>
    },
    ...
  ]
}
 
```
When a player searches for a new game, the application will fetch  ``waitingPlayers`` and map it to a data class. ``playerPairs`` will then be iterated through, and two events can occur:
1) There are no items in the array for which the ``player2`` field is empty, which will result in a new item being added to ``playerPairs`` in Firestore. ``player1`` will be the username of the searching player, and ``player2`` will be left empty. A random UID will also be associated with this new item. A snapshot listener will then wait for the creation of a Game document in the ``games`` collection whose ID matches the ID of the newly instantiated ``PlayerPair`` object. The client can obtain details about the game from this new document, such as the name of the opponent and which player goes first.
2) An item is found for which ``player2`` is empty. The ``playerPair`` of the matched players will be deleted from Firestore and a new Game document will be created in the ``games`` collection. The new Game document's identifier is the ID field from the erased player pair.

These conditions and their consequent actions are executed as a Firestore transaction. This means that if the condition that triggered a series of tasks is no longer true before their completion, the actions underway are completely undone and the transaction is run again. This ensures that no race conditions occur.

# Playing the Game 

A Game document is structured as follows:

```
<uid>  {
  currentTurn: <username>,
  lastPlay: <index 0-8 or -1 if the game just started>,
  players: [
    0 : <player 1 username>,
    1 : <player 2 username> 
  ],
  status: <"started" or "ended">,
  winner: <the winner's username or "none" for a draw>
}
```

After two players match, their respective clients attach a snapshot listener to the appropriate Game object and populate their ``GameViewModel`` locally with metadata about the game. On each field change, their listener checks to see whether the ``currentTurn`` field is the same as their username. If so, the RecyclerView representing the tic-tac-toe board  will place the opponent's X or O in the square whose index matches the ``lastPlay`` field. The user can then make their own move, updating ``lastPlay`` and ``CurrentTurn`` so that the whole process is repeated on the opponent's side. 

When a player makes a move which triggers a win or draw, ``status`` will be updated along with the aforementioned fields, signaling the end of the game to both users. Each player can then choose to play another game, restarting the whole endeavor.


# Application Architecture 

This application utilizes a design pattern in which the navigation between fragments is achieved through callbacks implemented in a single activity. A fragment declares an interface comprised of methods that it will use to navigate to other fragments. Their implementation takes place in ``TicTacToeActivity``, thereby keeping fragment transaction details separate from the fragments themselves. The fragment can use the appropriate navigation methods through a callback object, which downcasts the ``TicTacToeActivity`` context to the proper interface.

The application is organized into 3 layers:

```
Fragment
   |
ViewModel
   |
Repository 
```

The repository layer consists of methods which directly call Firestore or Firebase. They return ``Task`` objects containing the result of the query. The ViewModel methods call repository-level functions and dictate what action should be taken based on the success or failure of the ``Task`` objects they return. In response to a UI event (a button click or a move made on the tic-tac-toe board), the Fragment layer will call the appropriate ViewModel method, the result of which will propagate back up via observed LiveData.






