# Report

---

## Data Structures

Each user has a timestamp vector that is updated whenever they send a message.
Each entry of the timestamp vector is for a server and is the time that the user last received a message from that server.
The timestamp vector is initialized to all zeros. When a user sends a message, the timestamp vector is updated according to the
timestamp of the server that the message was sent. 

Each server has a timestamp vector that is updated whenever a message is received.
ReplicaTS is updated whenever an operation is received by the server
ValueTS is updated (merged with clientTS) whenever an operation is stable and executed by the server

The timestamp vector is implemented in the VectorClock class, and it's used by all the servers and clients.

---

## Number of Servers

The system currently supports 2 concurrent servers. Adding a third or N more servers would require deep modifications to the code (mainly
the propagateState method).

---

## PropagateState Optimization

Each server has a ledger and a ledgerReplica. Whenever a server receives a message, it updates its ledger and ledgerReplica.
The ledgerReplica is used to propagate the state to the other servers. Everytime a server propagates its state, it sends the ledgerReplica
and immediately cleans it. This is done to avoid sending operations that are already stable and executed on all servers. 

---

## Design decisions

In the last version of the project, the serverState was responsible for executing the operations and updating the ledger and the
accounts Map. This was a problem because it became difficult to distinguish between operation types when iterating the whole ledger.
To solve this problem, I created a new abstract method in the Operation class called execute. This method is implemented by each
operation type, and it receives and is responsible for updating the accounts Map. This way, the serverState is only responsible
for updating the ledger and calling the execute method of each operation.

We decided to split each operation by Wrappers and Receivers. The wrappers are called when the client sends an operation to the server,
and the receivers are called when the server receives an operation from another server propagating its state. This way it is easier
to debug and implement different behaviour for each case.

---

## Debug options

We updated the debug options to be more useful. Now, after every user sent remote call, its timestamp vector is printed.
And after every operation that is executed by the server is printed, the server Value and Replica timestamp vectors are printed.
When a server is unable to execute an operation received by a propagation state request, the error is printed to the servers console.