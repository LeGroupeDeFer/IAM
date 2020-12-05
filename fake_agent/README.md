# Fake agent

A simple utility meant to emulate the behavior of actual trash cans. Its main purpose is to to allow the development of backend/frontend functionalities without having to purchase/configure an actual agent.

## How to

For the following examples, the IAM server is assumed to be running on the local machine (_localhost_) and listening on port _8000_. Similarly, the fictional administrator account _john_ with password _secret_ will be used. Feel free to change the parameters if/when your installation details differ. 

### Generate an agent

Creating an agent named _hermes_, located in Namur city center (BE):
```bash
python3 -m fake_agent generate -h localhost --port 8000 -u john -p secret -n hermes 50.4667:4.8667:0.008
```
Where `50.4667:4.8667:0.008` stands for `latitude`:`longitude`:`radius`, which imply that the generated can will be located somewhere between 50,4587, 50,4747  (latitude) and 4,8587, 4,8747 (longitude).

### Running an agent
Running hermes:
```bash
python3 -m fake_agent run -n hermes
```
At which point the fake _hermes_ agent will be running and periodically update your server with fake filling rate information.

### Generating N agents

Generating 6 agents whose names start with _bestagon_:
```bash
python3 -m fake_agent generate -h localhost --port 8000 -u john -p secret -n bestagon -c 6 50.4667:4.8667:0.008
```

### Running all agents

Running _hermes_ and the _bestagons_:
```bash
python3 -m fake_agent run
```

## Note
Be sure that the desired agent names are not already in use on your installation prior to generation. This utility does not check for name (identifier) conflicts prior to agent subscribption (It will fail however and clean locally created files when such issue occurs).