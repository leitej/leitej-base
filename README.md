# leitej-base

This is the base of leitej framework, is written to run in Pure Java enviroment, with the mind set to help develop faster and concise Java Application. Allows using a simpler method to implement multi-thread, offers an agnostic way of modeling objects in XML to stream. And offers a great packages with standard tools.

### Description of modules (working on)

#### XML Object Modeling
```java
package leitej.xml.om;
```

#### Long Term Memory
```java
package leitej.ltm;
```
A simple and easy way of save data states between different process executions.<br/>
Data structure has to be defined by an interface extending LtmObjectModelling.

```java
public interface Message extends LtmObjectModelling {
	String getText();
	void setText(String text);
}
```
Then use it, like:
```java
private static final LongTermMemory LTM = LongTermMemory.getInstance();

Message msg = LTM.newRecord(Message.class);
// msg.getLtmId(); // data ID in long term memory
msg.setText("Hi!");
```
Done!

In the next execution check the state:
```java
// When you know the long term memory ID
assertTrue(LTM.fetch(Message.class, ID).getText().equals("Hi!"));

// If you want to find it
LtmFilter<Message> filter = new LtmFilter<>(Message.class, OPERATOR_JOIN.AND);
filter.append(OPERATOR.LIKE).setText("%Hi%");

Iterator<Message> result = LTM.search(filter);
assertTrue(result.next().getText().equals("Hi!"));
```

#### Some catchs to use cryptography
```java
package leitej.crypto;
```

#### Process communication layer
```java
package leitej.net;
```

#### Parallel method invocation
```java
package leitej.thread;
```

#### Useful classes in various contexts
```java
package leitej.util;
```

## Distribution

You can find the compiled code and dependencies in [dist](./dist/). And the API can be consulted [here](https://leitej.github.io/leitej-base).

### Prerequisites

Java Runtime Environment installed.

## License

This project is licensed under the GNU General Public License v3.0 License - see the [LICENSE](LICENSE) file for details
