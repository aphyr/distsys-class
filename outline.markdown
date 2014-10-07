# Crashlanding in the Asteroid Belt

## An introduction to distributed systems

This course aims to introduce software engineers to the practical basics of
distributed systems. We'll keep the course small enough so that everyone can
talk, and include plenty of time for discussion and back-and-forth learning.
Participants will gain an intuitive understanding of key distributed systems
terms, have a toolkit for reasoning about their own consensus and ordering
problems, and understand the landscape of modern distributed systems.



## What makes a thing distributed?

Lamport, 1987:

>  A distributed system is one in which the failure of a computer
>  you didn't even know existed can render your own computer
>  unusable.

- First glance: \*nix boxen in our colo, running processes communicating via
  TCP or UDP.
  - Or boxes in EC2, rackspace, etc
  - Maybe communicating over infiniband
  - Separated by inches and a LAN
  - Or by kilometers and the internet
- Most mobile apps are also taking part in a distributed system
  - Communicating over a truly awful network
  - Same goes for desktop web browsers
  - It's not just servers--it's clients too!
- More generally: distributed systems are
  - Made up of parts
  - Which interact
  - Slowly
  - Whatever "slow" means for you
- So also:
  - X86 NUMA architectures
  - ATMs and Point-of-Sale terminals
  - Paying bills
  - Doctors making referrals
  - Drunk friends texting trying to make plans via text message
  - Every business meeting ever
  - Space probes
  - Hanging up first

## Nodes and networks

- We call each part of a distributed system a *node*, *process*, *agent*, or
  *actor*.

### Nodes as linearization points

- A node could *itself* be a distributed system
  - But for the purposes of analysis, we say it's a single coherent process
  - Things on a node appear to occur in a well-defined order
  - Typically modeled as some kind of single-threaded state machine
- Formal models
  - Crash-stop
  - Crash-recover
  - Byzantine
- Characteristic latency
  - Operations inside a node are "fast"
  - Operations between nodes are "slow"
  - What's fast or slow depends on what the system does

### Networks as message flows

- Nodes interact via a *network*
  - Humans interact via spoken words
  - Particles interact via fields
- We model those interactions as discrete *messages* sent between nodes
- Messages take *time* to propagate
  - This is the "slow" part of the distributed system
  - We call this "latency"

### Causality diagrams

- We can represent the interaction of nodes and the network as a diagram
  - Time flows up
  - Nodes are worldlines separate by space
  - Messages as lines *connecting* nodes

### Synchronous networks

- Execute in lockstep
- Fixed bounds on transmission delay
- Accurate global clock
- Easy to prove stuff about
  - You probably don't have one

### Asynchronous networks

- Execute independently, whenever
- Unbounded message delays
- No global clocks
- IP networks are definitely asynchronous
  - But *in practice* the really pathological stuff doesn't happen
  - Most networks recover in seconds to weeks, not "never"
    - Conversely, human timescales are on the orders of seconds to weeks
    - So we can't pretend the problems don't exist



## When networks go wrong

- Asynchronous networks are allowed to
  - Duplicate
  - Delay
  - Drop
  - Reorder
- Byzantine networks are allowed to mess with messages *arbitrarily*
  - Including rewriting their content
  - They mostly don't happen in real networks
    - Mostly
- Drops and delays are indistinguishible

## Low level protocols

### TCP

- TCP *works*. Use it.
  - Not perfect; you can go faster
  - But you'll know when this is the case
- In practice, TCP prevents duplicates and reorders in the context of a single
  TCP conn
  - But you're probably gonna open more than one connection
  - If for no other reason than TCP conns eventually fail
  - And when that happens, you'll either a.) have missed messages or b.) retry
  - You can *reconstruct* an ordering by encoding your own sequence numbers on
    top of TCP

### UDP

- Same addressing rules as TCP, but no stream invariants
- Lots of people want UDP "for speed"
  - Don't consider that routers and nodes can and will arbitrarily drop packets
  - Don't consider that their packets *will* be duplicated
  - And reordered
  - "But at least it's unbiased right?"
    - WRONG!
  - This causes all kinds of havoc in, say, metrics collection
  - And debugging it is *hard*
  - TCP gives you flow control and repacks logical messages into packets
    - You'll need to re-build flow-control and backpressure
  - TLS over UDP is a thing, but tough
- UDP is really useful where TCP FSM overhead is prohibitive
  - Memory pressure
  - Lots of short-lived conns and socket reuse
- Especially useful where best-effort delivery maps well to the system goals
  - Voice calls: people will apologize and repeat themselves
  - Games: stutters and lag, but catch up later
  - Higher-level protocols impose sanity on underlying chaos



## Clocks

- When a system is split into independent parts, we still want some kind of
  *order* for events

### Wall Clocks

- In theory, a process clock gives you a partial order on system events
  - Caveat: NTP is probably not as good as you think
  - Caveat: Definitely not well-synced between nodes
  - Caveat: Hardware can drift
  - Caveat: Administrators can drift
  - Caveat: By *centuries*
  - Caveat: POSIX time is not monotonic by *definition*
  - Caveat: The timescales you want to measure may not be attainable
  - Caveat: Threads can sleep
  - Caveat: Runtimes can sleep
  - Caveat: OS's can sleep
  - Caveat: "Hardware" can sleep
- Just don't.

### Lamport Clocks

- Lamport 1977: "Time, Clocks, and the Ordering of Events in a Distributed System"
  - One clock per process
  - Increments monotonically with each state transition: `t' = t + 1`
  - Included with every message sent
  - `t' = max(t, t_msg + 1)`
- If we have a total ordering of processes, we can impose a total order on
  events
  - But that order could be pretty unintuitive

### Vector Clocks

- Generalizes Lamport clocks to a vector of all process clocks
- `t_i' = max(t_i, t_msg_i)`
- For every operation, increment that process' clock in the vector
- Provides a partial order
  - Specifically, given a pair of events, we can determine causal relationships
    - Independent
    - A in causal past of B
    - B in causal past of A
- Pragmatically: the past is shared; the present is independent
  - Only independent states need to be preserved
  - States which led to new states can be discarded
  - Lets us garbage-collect the past
- O(processes) in space
  - Requires coordination for GC
  - Or sacrifice correctness and prune old entries

### Dotted Version Vectors

- Basically: better vector clocks
- Still a partial order, but orders *more* events
- Reduces issues with sibling explosion

### GPS & Atomic Clocks

- Much better than NTP
  - Globally distributed total orders on the scale of milliseconds
  - Can do one possibly conflicting thing per uncertainty window
- Only people with this right now are Google Spanner
  - And they're not sharing
- More expensive than you'd like
  - Vendors can get it wrong
    - Need multiple sources: vendors can get it wrong
  - I don't know who's doing it yet, but I'd bet dollars to donuts every major
    datacenter in the future will offer dedicated HW interfaces for
    bounded-accuracy time.



## Availability

- Availability is the set of successful operations out of attempted operations

### Total availability

- Naieve: every operation succeeds
- In consistency lit: every operation against a non-failing node succeeds
  - Nothing you can do about the failing nodes

### Sticky availability

- Every operation against a non-failing node succeeds
  - With the constraint that clients always talk to the same nodes

### Quantifying availability

- We talk a lot about "uptime"
  - Are systems up if nobody uses them?
  - Is it worse to be down during peak hours?
  - Can measure "fraction of requests satisfied during a time window"
  - Then plot that fraction over windows at different times
  - Timescale affects reported uptime
- Apdex
  - Not all successes are equal
  - Classify operations into "OK", "meh", and "awful"
  - Apdex = P(OK) + P(meh)/2
  - Again, can report on a yearly basis
    - "We achieved 99.999 apdex for the year"
  - And on finer timescales!
    - "Apdex for the user service just dropped to 0.5; page ops!"
- Ideally: integral of suffering inflicted by your service?


## Consistency

### Monotonic Reads

- Once I read a value, any successive read will return a causally consequent
  state from that read.

### Monotonic Writes

- If I make a write, any subsequent writes I make will take place against a
  causally consequent value from my prior write.

### Read Your Writes

- Once I write a value, any successive read will return a causally consequent
  state from that write.

### Writes Follow Reads

- Once I read a value, any successive write will take place against a causally
  consequent value from the read.

### Serializability

- All operations appear to execute atomically

### Causal consistency

- Suppose operations can be linked by a DAG of causal relationships
  - A write that follows a read, for instance, is causally related
    - Assuming the process didn't just throw away the read data
  - Operations not linked by DAG are *concurrent*
- Constraint: all processes agree on the order of operations that are causally
  related
- Concurrent ops can be freely reordered

### Sequential consistency

- All operations appear to execute atomically
- Every process agrees on the order of operations

### Linearizability

- All operations appear to execute atomically
- Every process agrees on the order of operations
- Every operation appears to take place *between* its invocation and completion

### ACID isolation levels

- ANSI SQL's ACID isolation levels are weird
  - Basically codified whatever weird tradeoffs the various vendors were making
  - Definitions in the spec are ambiguous
    - Two interpretations
      - *anomaly interpretation*
      - *preventative interpretation* (stronger)
- Adya 1999: Weak Consistency: A Generalized Theory and Optimistic
  Implementations for Distributed Transactions
  - Read Uncommitted
    - Prevents P0: *dirty writes*
      - w1(x) ... w2(x)
      - Can't write over another transaction's data until it commits
    - Can read data while a transaction is still modifying it
    - Can read data that will be rolled back
  - Read Committed
    - Prevents P1: *dirty reads*
      - w1(x) ... r2(x)
      - Can't read a transaction's uncommitted values
  - Repeatable Read
    - Prevents P2: *fuzzy reads*
      - r1(x) ... w2(x)
      - Once a transaction reads a value, it won't change until the transaction
        commits
  - Serializable
    - Prevents P3: *phantoms*
      - Given some predicate P
      - r1(P) ... w2(y in P)
      - Once a transaction reads a set of elements satisfying a query, that
        set won't change until the transaction commits
      - Not just values, but *which values even would have participated*.
  - Cursor Stability
    - Transactions have a set of cursors
      - A cursor refers to an object being accessed by the transaction
    - Read locks are held until cursor is removed, or commit
      - At commit time, cursor is upgraded to a writelock
    - Prevents lost-update
  - Snapshot Isolation
    - Transactions always read from a snapshot of committed data a logical time
      when the transaction begins
    - Commit can only occur if no other transaction with an overlapping
      [start..commit] interval has written to any of the objects written



## Tradeoffs

- Consistency requires coordination
  - If every order is allowed, we don't need to do any work!
  - If we want to disallow some orders of events, we have to exchange messages
- Coordinating comes (generally) with costs
  - More consistency is slower
  - More consistency is more intuitive
  - More consistency is less available

### Availability and Consistency

- CAP Theorem: Linearizable systems cannot have total availability
- But wait, there's more!
  - Bailis 2014: Highly Available Transactions: Virtues and Limitations
  - Other theorems disallow totally or sticky available...
    - Strong serializable
    - Serializable
    - Repeatable Read
    - Cursor Stability
    - Snapshot Isolation
  - You can have *sticky* available...
    - Causal
    - PRAM
    - Read Your Writes
  - You can have *totally* available...
    - Read Uncommitted
    - Read Committed
    - Monotomic Atomic View
    - Writes Follow Reads
    - Monotonic Reads
    - Monotonic Writes


### Harvest and Yield

- Fox & Brewer, 1999: Harvest, Yield, and Scalable Tolerant Systems
  - Yield: probability of completing a request
  - Harvest: fraction of data reflected in the response
  - Examples
    - Node faults in a search engine can cause some results to go missing
    - Updates may be reflected on some nodes but not others
      - Consider an AP system split by a partition
      - Eventually things will resolve
      - But meanwhile, other nodes can do reads that won't see the new data
    - Streaming video degrades to preserve low latency
  - This is not an excuse to violate your safety invariants
    - Just helps you quantify how much you can *exceed* safety invariants
    - e.g. "We provide RYW 99.9% of the time"
  - Strongly dependent on workload, HW, topology, etc
  - Can tune harvest vs yield on a per-request basis
   - "As much as possible in 10ms, please"

### Hybrid systems

- So, you've got a spectrum of choices!
  - Chances are different parts of your infrastructure have different needs
  - Pick the weakest model that meets your constraints
    - But consider probabilistic bounds; visibility lag might be prohibitive
    - See Probabilistically Bounded Staleness in Dynamo Quorums
- Not all data is equal
  - Big data is usually less important
  - Small data is usually critical
  - Linearizable user ops, causally consistent social feeds

## Gossip

- Useful for service discovery, performance tuning, self-healing, etc
- Very weak consistency
- Very high availability

### Global broadcast

- Send a message to every other node
- O(nodes)

### Mesh networks

- Epidemic models
- Relay to your neighbors
- Propagation times on the order of max-free-path

### Spanning trees

- Instead of a mesh, use a tree
- Hop up to a connector node which relays to other connector nodes
- Reduces superfluous messages
- Reduces latency
- Plumtree (Leit ̃ao, Pereira, & Rodrigues, 2007: Epidemic Broadcast Trees)


## Avoid Consensus Wherever Possible

### CALM conjecture

- Consistency As Logical Monotonicity
  - If you can prove a system is logically monotonic, it is coordination free
  - What the heck is "coordination"
  - For that matter, what's "monotonic"?
- Montonicity, informally, is retraction-free
  - Deductions from partial information are never invalidated by new information
  - Both relational algebra and (some) Datalog are monotone
- Ameloot, et al, 2011: Relational transducers for declarative networking
  - Theorem which shows coordination-free networks of processes unaware of the
    network extent can only compute only monotone queries in Datalog.
  - Honestly, the formalism of temporal logic is still pretty tough for me
  - "Coordination-free" doesn't mean no communication
    - Algo succeeds even in face of arbitrary horizontal partitions
- Bloom language
  - Unordered programming with flow analysis
  - Can hint where coordination *would* be required

### CRDTs

- Order-free datatypes that converge
- Tolerate dupes, delays, and reorders
- Unlike sequentially consistent systems, no "single source of truth"
- But unlike naive eventually consistent systems, never *lose* information
  - Unless you explicitly make them lose information
- Works well in highly-available systems
  - Web/mobile clients
  - Dynamo
  - Gossip
- INRIA: Shapiro, Preguiça, Baquero, Zawirski, 2011: "A comprehensive study of
  Convergent and Commutative Replicated Data Types"
  - Composed of a data type X and a merge function m, which is:
    - Associative: m(x1, m(x2, x3)) = m(m(x1, x2), x3)
    - Commutative: m(x1, x2) = m(x2, x1)
    - Idempotent:  m(x1, x1) = m(x1)

### HATs

- Bailis, Davidson, Fekete, et al, 2013: "Highly Available Transactions,
  Virtues and Limitations"
  - Guaranteed responses from any replica
  - Low latency (1-3 orders of magnitude faster than serializable protocols!)
  - Read Committed
  - Monotonic Atomic View
  - Excellent for commutative/monotonic systems
  - Foreign key constraints for multi-item updates
  - Limited uniqueness constraints
  - Can ensure convergence given arbitrary finite delay ("eventual consistency)
  - Good candidates for geographically distributed systems
  - Probably best in concert with stronger transactional systems
  - See also: COPS, Swift, Eiger, etc

## Fine, We Need Consensus, What Now?

- The consensus problem:
  - Three process types
    - Proposers: propose values
    - Acceptors: choose a value
    - Learners: read the chosen value
  - Classes of acceptors
    - N acceptors total
    - F acceptors allowed to fail
    - M malicious acceptors
  - Three invariants:
    - Nontriviality: Only values proposed can be learned
    - Safety: At most one value can be learned
    - Liveness: If a proposer p, a learner l, and a set of N-F are non-faulty
      and can communicate with each other, and if p proposes a value, l will
      eventually learn a value.

- Whole classes of systems are *equivalent* to the consensus problem
  - So any proofs we have here apply to those systems too
  - Lock services
  - Ordered logs
  - Replicated state machines

- FLP tells us consensus is impossible
  - Kill a process at the right time and you can break *any* consensus algo
  - Moreover, FLP assumes deterministic processes
    - Ben-Or 1983: "Another Advantage of free choice"
      - Nondeterministic algorithms *can* achieve consensus

- Lamport 2002: tight bounds for asynchronous consensus
  - With at least two proposers, or one malicious proposer, N > 2F + M
    - "Need a majority"
  - With at least 2 proposers, or one malicious proposer, it takes at least 2
    message delays to learn a proposal.

- This is a pragmatically achievable bound
  - In stable clusters, you can get away with only a single round-trip to a
    majority of nodes.
  - More during cluster transitions.


### VR

### Paxos

### ZAB

### Raft




## Characteristic latencies

### Multicore systems

### Local networks

### Geographic replication




## A Pattern Language

### Don't distribute

### Never fail

### Accept failure

### Redundancy

- Redundancy improves availability so long as failures are uncorrelated
  - Failures are not uncorrelated

### Sharding

### Immutable values

### Mutable identities

### Strong Metadata, cheap storage




## Production Systems

### Services for domain models

### Structure Follows Social Spaces

### Test everything

### "It's Slow"

### Instrument everything

### Versioning and rollouts

### Feature flags

### Shadow traffic

### Oh God, queues
