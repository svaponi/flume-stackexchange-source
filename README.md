# Flume StackExchange Source
The StackExchange source queries StackExchange API and passes information to the flume agent to which it belongs.

### Configuration
This is an example of `flume-conf.properties` used for testing this source.

```
# Configuration for Flume agent a1
a1.sources = s1
a1.channels = c1
a1.sinks = k1

# This is standard Source config
a1.sources.s1.type = it.miriade.flume.source.stackexchange.StackExchangeSource
a1.sources.s1.name = Miriade-FlumeStackExchangeSource

# This is StackExchange Source config
a1.sources.s1.stackexchange.api.poller.fixedrate = 2500
a1.sources.s1.stackexchange.api.poller.timeunit = MILLISECONDS

# Specify the channel the source should use
a1.sources.s1.channels = c1

# This channel works in memory, it's use for testing
a1.channels.c1.type = memory
a1.channels.c1.capacity = 100000

# This sink writes on a file in /tmp directory, it's use for testing
a1.sinks.k1.type = file_roll
a1.sinks.k1.sink.directory = /tmp
a1.sinks.k1.rollInterval = 60

# Specify the channel the sink should use
a1.sinks.k1.channel = c1
```

### About Flume
For further information see [http://flume.apache.org/FlumeUserGuide.html](http://flume.apache.org/FlumeUserGuide.html).
