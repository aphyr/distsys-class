# Crashlanding in the Asteroid Belt

## An introduction to distributed systems

This course aims to introduce software engineers to the practical basics of
distributed systems. We'll keep the course small enough so that everyone can
talk, and include plenty of time for discussion and back-and-forth learning.
Participants will gain an intuitive understanding of key distributed systems
terms, have a toolkit for reasoning about their own consensus and ordering
problems, and understand the landscape of modern distributed systems.

## Outline

- What makes a thing distributed?
- Nodes and networks
  - Nodes as linearization points
  - Networks as message flows
  - Synchronous networks
  - Asynchronous networks
  - Causality diagrams
- When networks go wrong
  - Duplication
  - Delays
  - Drops
  - Reordering
- Low-level protocols
  - IP
  - TCP
  - UDP
- Clocks
  - Wall Clocks
  - Lamport Clocks
  - Vector Clocks
  - Atomic Clocks
- Availability
  - Total availability
  - Sticky availability
  - Partial availability
- Consistency
  - Causal consistency
  - Sequential consistency
  - Serializability
  - Linearizability
  - ACID isolation levels
- Tradeoffs
  - CAP and other limits
  - Harvest and Yield
  - Hybrid systems
- Gossip
  - Global broadcast
  - Homogenous networks
  - Spanning trees
- Avoid Consensus Wherever Possible
  - CRDTs
  - HATs
- Fine, We Need Consensus, What Now?
  - Paxos
  - ZAB
  - Raft
  - VR
- Characteristic latencies
  - Multicore systems
  - Local networks
  - Geographic replication
- A Pattern Language
  - Don't distribute
  - Never fail
  - Accept failure
  - Redundancy
  - Sharding
  - Immutable values
  - Mutable identities
  - Strong Metadata, cheap storage
- Production Systems
  - Services for domain models
  - Structure Follows Social Spaces
  - Test everything
  - "It's Slow"
  - Instrument everything
  - Versioning and rollouts
  - Feature flags
  - Shadow traffic
  - Oh God, queues
