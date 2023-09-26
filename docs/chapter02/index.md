---
title: "JavaConcurrencyInPractice-chapter02-线程安全性"
date: 2023-09-20T19:00:10+08: 00
draft: false
tags: ["thread safety"]
categories: ["concurrency", "java"]
twemoji: true
lightgallery: true
---
你或许会感到奇怪，线程或者锁在并发编程中的作用，类似于铆钉和工字梁在土木工程中的作用。要建筑一座坚固的桥梁，必须正确地使用大量的铆钉和工字梁。同理，在构建稳健的并发程序时，必须正确地使用线程和锁。但这些终归只是一些机制。 **要编写线程安全的代码,其核心在于要对状态访问操作进行管理，特别是对&nbsp;<i>共享的(Shared)</i>&nbsp;和&nbsp;<i>可变的(Mutable)</i>&nbsp;状态的访问。**

从非正式的意义上来说, **对象的状态是指存储在状态变量(例如实例或静态域)中的数据**. 对象的状态可能包括其他依赖对象的域。例如，某个 HashMap 的状态不仅存储在HashMap 对象本身，还存储在许多 Map.Entry 对象中。在对象的状态中包含了任何可能影响其外部可见行为的数据。“共享”意味着变量可以由多个线程同时访问，而“可变”则味着变量的值在其生命周期内可以发生变化。我们将像讨论代码那样来讨论线程安全性，但更侧重于如何防止在数据上发生不受控的并发访问。

**一个对象是否需要是线程安全的, 取决于它是否被多个线程访问。这指的是在程序中访问对象的方式，而不是对象要实现的功能。要使得对象是线程安全的，需要采用同步机制来协同对对象可变状态的访问。如果无法实现协同，那么可能会导致数据破坏以及其他不该出现的结果。**

当多个线程访问某个状态变量并且其中有一个线程执行写入操作时，必须采用同步机制来协同这些线程对变量的访问。Java 中的主要同步机制是关键字 synchronized，它提供了一种独占的加锁方式，但“同步”这个术语还包括 volatile 类型的变景，显式锁 (Explicit Lock)以及原子变量。在上述规则中并不存在一些想象中的“例外”情况。即使在某个程序中省略了必要同步机制并且看上去似乎能正确执行，而且通过了测试并在随后几年时间里都能正确地执行，但程序仍可能在某个时刻发生错误。

如果当多个线程访问同一个可变的状态变量时没有使用合适的同步，那么程序就会出现错误。有三种方式可以修复这个问题:
1. 不在线程之间共享该状态变量。
2. 将状态变量修改为不可变的变量。
3. 在访问状态变量时使用同步。

如果在设计类的时候没有考虑并发访问的情况，那么在采用上述方法时可能需要对设计进行重大修改，因此要修复这个问题可谓是知易行难。如果从一开始就**设计一个线程安全的类**, 那么比在以后再将这个类修改为线程安全的类要容易得多。在一些大型程序中，要找出多个线程在哪些位置上将访问同一个变量是非常复杂的。幸运的是，**面向对象这种技术不仅有助于编写出结构优雅、可维护性高的类，还有助于编写出线程安全的类**。访问某个变量的代码越少，就越容易确保对变量的所有访问都实现正确同步，同时也更容易找出变量在哪些条件下被访问。Java 语言并没有强制要求将状态都封装在类中，开发人员完全可以将状态保存在某个公开的域(甚至公开的静态域)中，或者提供一个对内部对象的公开引用。然而，程序状态的封装性越好，就越容易实现程序的线程安全性，并且代码的维护人员也越容易保持这种方式。

**当设计线程安全的类时, 良好的面向对象技术、不可修改性, 以及明晰的不变性规范都能起到一定的帮助作用。**

### 2.1 何为线程安全性
要对线程安全性给出一个确切的定义是非常复杂的。定义越正式，就越复杂，不仅很难提供有实际意义的指导建议，而且也很难从直观上去理解。因此，下面给出了一些非正式的描述，看上去令人困惑。在互联网上可以搜索到许多“定义”，例如: 
* 可以在多个线程中调用，并且在线程之间不会出现错误的交互。
* 可以同时被多个线程调用，而调用者无须执行额外的动作。

看看这些定义，难怪我们会对线程安全性感到困惑。它们听起来非常像“如果某个类可以在多个线程中安全地使用，那么它就是一个线程安全的类”。对于这种说法，虽然没有太多的争议，但同样也不会带来太多的帮助。我们如何区分线程安全的类以及非线程安全的类?进一步说，“安全”的含义是什么?
在线程安全性的定义中，最核心的概念就是**正确性**。如果对线程安全性的定义是模糊的那么就是因为缺乏对正确性的清晰定义。
**正确性的含义是，某个类的行为与其规范完全一致。** 在良好的规范中通常会定义各种 **不变性条件(Invariant)** 来约束对象的状态，以及定义各种 **后验条件(Postcondition)** 来描述对象操作的结果。由于我们通常不会为类编写详细的规范，那么如何知道这些类是否正确呢?我们无法知道，但这并不妨碍我们在确信“类的代码能工作”后使用它们。这种“代码可信性”非常接近于我们对正确性的理解，因此我们可以将单线程的正确性近似定义为“**所见即所知 (we know it when we see it)**”。在对“正确性”给出了一个较为清的定义后，就可以定义线安全性: **当多个线程访问某个类时，这个类始终都能表现出正确的行为，那么就称这个类是线程安全的**。

> 示例:一个无状态的 Servlet
我们在第1章列出了一组框架，其中每个框架都能创建多个线程并在这些线程中调用你编写的代码，因此你需要保证编写的代码是线程安全的。通常，线程安全性的需求并非来源于对线程的直接使用，而是使用像 Servlet 这样的框架。我们来看一个简单的示例---一个基于Servlet 的因数分解服务，并逐渐扩展它的功能，同时确保它的线程安全性。
程序清单2-1给出了一个简单的因数分解 Servlet。这个Servlet 从请求中提取出数值，执行因数分解，然后将结果封装到该 Servlet 的响应中。
```java
public class StatelessFactorizer implements Servlet {
    public void service(ServletRequest req，ServletResponse resp){
        BigInteger i = extractFromRequest(req);
        BigInteger[] factors = factor(i);
        encodeIntoResponse(resp, factors);
    }
}
```
**与大多数 Servlet相同，StatelessFactorizer是无状态的:它既不包含任何域，也不包含任何对其他类中域的引用。** 计算过程中的临时状态仅存在于线程栈上的局部变量中，并且只能由正在执行的线程访问。访 StatelessFactorizer 的线程不会影响另一个访问同一个 StatelessFactorizer 的线程的计算结果，因为这两个线程并没有共享状态，就好像它们都在访问不同的实例。由于线程访问无状态对象的行为并不会影响其他线程中操作的正确性，因此无状态对象是线程安全的。
**无状态对象一定是线程安全的**, 大多数 Servlet 都是无状态的，从而极大地降低了在实现 Servlet 线程安全性时的复杂性只有当 Servlet 在处理请求时需要保存一些信息，线程安全性才会成为一个问题.

### 2.2 原子性
当我们在无状态对象中增加一个状态时, 会出现什么情况? 假设我们希望增加一个“命中计数器”(Hit Counter) 来统计所处理的请求数量. 一种直观的方法是在 Servlet, 程序清单2-2 在没有同步的情况下统计已处理请求数量的 Servlet (不要这么做)
```java
public class UnsafeCountingFactorizer implements Servlet{
    private long count = 0;
    public long getCount(){
        return count ; 
    }
    public void service(ServletRequest req,ServletResponse resp){
        BigInteger i = extractFromRequest(reg);
        BigInteger[] factors = factor(i);
        ++count;
        encodeIntoResponse(resp, factors);
    }
}
```
不幸的是, `UnsafeCountingFactorizer` 并非线程安全的, 尽管它在单线程环境中能正确运行与前面的 `UnsafeSequence` 一样, 这个类很可能会丢失一些更新操作. 虽然递增操作 ++count 是一种紧凑的语法, 使其看上去只是一个操作, 但这个操作并非原子的, 因而它并不会作为一个不可分割的操作来执行. **实际上, 它包含了三个独立的操作: 读取 count 的值, 将值加1, 然后将计算结果写入 count. 这是一个 "读取 - 修改 - 写入" 的操作序列, 并且其 <i>结果状态依赖于之前的状态.</i>**
图 1-1给出了两个线程在没有同步的情况下同时对一个计数器执行递增操作时发生的情况: 如果计数器的初始值为 9, 那么在某些情况下, 每个线程得到的值都为 9, 接着执行递增操作并且都将计数器的值设为 10. 显然, 这并不是我们希望看到的情况, 如果有一次递增操作丢失了, 命中计数器的值就将偏差 1。

#### 2.2.1 竞态条件
在 `UnsafeCountingFactorizer` 中存在多个**竞态条件**, 从而使结果变得不可靠. 当某个计算的正确性取决于多个线程的交替执行时序时, 那么就会发生竞态条件. 换句话说, 就是正确的结果要取决于运气. 最常见的竞态条件类型就是 **"先检查后执行(Check-Then-Act)"** 操作，即**通过一个可能失效的观测结果来决定下一步的动作**。

在实际情况中经常会遇到竞态条件。例如，假定你计划中午在 University Avenue 的星巴克与一位朋友会面。但当你到达那里时，发现在 University Avenue 上有两家星巴克，并且你不知道说好碰面的是哪一家。在 12:10 时，你没有在星巴克 A 看到朋友，那么就会去星巴克B 看看他是否在那里，但他也不在那里。这有几种可能: 
1. 你的朋友迟到了, 还没到任何一家星巴克; 
2. 你的朋友在你离开后到了星巴克A;
3. 你的朋友在星巴克B, 但他去星巴克 A 找你, 并且此时正在去星巴克 A 的途中。

我们假设是最糟糕的情况，即最后一种可能。现在是 12:15, 你们两个都去过了两家星巴克, 并且都开始怀疑对方是否失约了. 现在你会怎么做? 回到另一家星巴克? 来来回回要走多少次? 除非你们之间约定了某种协议, 否则你们整天都在 University Avenue 上走来走去, 倍感沮丧. 在“我去看看他是否在另一家星巴克”这种方法中，问题在于: 当你在街上走时，你的朋友可能已经离开了你要去的星巴克。你首先看了看星巴克 A，发现“他不在”，并且开始去找他。你可以在星巴克 B 中做同样的选择，但不是同时发生。**两家星巴克之间有几分钟的路程, 而就在这几分钟的时间里, 系统的状态可能会发生变化.**在星巴克这个示例中说明了一种竟态条件, 因为要获得正确的结果(与朋友会面)，必须取决于事件的发生时序(当你们到达星巴克时, 在离开并去另一家星巴克之前会等待多长时间...)。**当你迈出前门时, 你在星巴克 A 的观察结果将变得无效, 你的朋友可能从后门进来了, 而你却不知道.** 

这种观察结果的失效就是大多数竞态条件的本质一: **基于一种可能失效的观察结果来做出判断或者执行某个计算**。这种类型的竞态条件称为“先检查后执行”:首先观察到某个条件为真(例如文件x不存在), 然后根据这个观察结果做出动作(创建文件x). 但事实上, 在你观察到这个结果以及开始创建文件之间, 观察结果可能变得无效(另一个线程在这期间创建了文件X), 从而导致各种问题(未预期的异常、数据被覆盖、文件被破坏等)。

#### 2.2.2 复合操作
`LazyInitRace` 和 `UnsafeCountingFactorizer` 都包含一组需要以原子方式执行(或者说不可分割)的操作。要避免竞态条件问题，就必须在某个线程修改该变量时，通过某种方式防止其他线程使用这个变量，从而**确保其他线程只能在修改操作完成之前或之后读取和修改状态，而不是在修改状态的过程中**。
```java
// don't do that
public class LazyInitRace {
    private ExpensiveObject instance = null;
    public ExpensiveObject getInstance() {
        if (instance == null)
            instance = new ExpensiveObject();
        return instance;
    }
}
```
如果 UnsafeSequence 中的递增操作是原子操作，那么图 1-1 中的竞态条件就不会发生，并且递增操作在每次执行时都会把计数器增加 1。为了确保线程安全性，“先检查后执行”(例如延迟初始化)和“读取一修改 - 写入”(例如递增运算)等操作必须是原子的。我们将“先检查后执行”以及“读取一 修改  写”等操作统称为复合操作:包含了一组必须以原子方式行的操作以确保线程安全性。在 2.3 节中，我们将介绍**加锁机制，这是 Java 中用于确保原子性的内置机制。** 就目前而言，我们先采用另一种方式来修复这个问题，即**使用一个现有的线程安全类**，如程序清单2-4中的 CountingFactorizer 所示:
```java
// 程序清单2-4使用 AtomicLong 类型的变量来统计已处理请求的数量
@Threadsafe
public class CountingFactorizer implements Servlet {
    private final AtomicLong count = new AtomicLong(0);
    public long getCount() {
        return count.get();
    }
    public void service(ServletRequest req，ServletResponse resp){
        BigInteger i = extractFromRequest(reg);
        BigInteger[] factors = factor(i);
        count.incrementAndGet();
        encodeIntoResponse(resp, factors);
    }
}
```
在 `java.util.concurrent.atomic` 包中包含了一些原子变量类，用于实现在数值和对象引用上的原子状态转换。通过用 `AtomicLong` 来代替 long 类型的计数器，能够确保所有对计数器状态的访问操作都是原子的。由于 Servlet 的状态就是计数器的状态，并且计数器是线程安全的，因此这里的 Servlet 也是线程安全的。我们在因数分解的 Servlet 中增加了一个计数器，并通过使用线程安全类 AtomicLong 来管理计数器的状态，从而确保了代码的线程安全性。**当在无状态的类中添加一个状态时，如果该状态完全由线程安全的对象来管理，那么这个类仍然是线程安全的。** 然而，在 2.3 节你将看到当状态变量的数量由一个变为多个时，并不会像状态变量数量由零个变为一个那样简单。

在实际情况中，应尽可能地使用现有的线程安全对象(例如 AcomicLong)来管理类的状态。与非线程安全的对象相比，判断线程安全对象的可能状态及其状态转换情况要更为容易，从而也更容易维护和验证线程安全性。

### 2.3 加锁机制
当在 Servlet 中添加一个状态变量时，可以通过线程安全的对象来管理 Servlet 的状态以护Servlet的线程安全性。但如果想在 Servlet 中添加更多的状态，那么是否只需添加更多的线程安全状态变量就足够了?
假设我们希望提升 Servlet 的性能: 将最近的计算结果缓存起来，当两个连续的请求对相同的数值进行因数分解时, 可以直接使用上一次的计算结果, 而无须重新计算. (这并非一种有效的缓存策略, 5.6节 将给出一种更好的策略)要实现该缓存策略, 需要保存两个状态:最近执行因数分解的数值, 以及分解结果. 我们曾通过 AtomicLong 以线程安全的方式来管理计数器的状态, 那么, 在这里是否可以使用类似的 AtomicReference 来管理最近执行因数分解的数值及其分解结果吗?在程序清单2-5中的 UnsafeCachingFactorizer 实现了这种思想.

`程序清单2-5: 该Servlet 在没有足够原子性保证的情况下对其最近计算结果进行缓存(不要这么做)`
```java
public class UnsafeCachingFactorizer implements Servlet{
    private final AtomicReference<BigInteger> lastNumber = new AtomicReference<BigInteger>();
    private final AtomicReference<BigInteger[]> lastFactors = new AtomicReference<BigInteger[]>();
    public void service(ServletRequest req,ServletResponse resp){
        BigInteger i = extractFromRequest (reg);
        if (i.equals(lastNumber.get())){
            encodeIntoResponse(resp, lastFactors.get());
        }else{
            BigInteger factors = factor(i);
            lastNumber.set(i);
            lastFactors.set(factors);
            encodeIntoResponse(resp, factors);
        }
    }
}
```

然而, 这种方法并不正确. 尽管这些原子引用本身都是线程安全的, 但在  `UnsafeCachingFactorizer` 中存在着竞态条件，这可能产生错误的结果。在线程安全性的定义中要求，多个线程之间的操作无论采用何种执行时序或交替方式，都要**保证不变性条件不被破坏**。 `UnsafeCachingFactorizer` 的不变性条件之一是: **在 `lastFactors` 中缓存的因数之积应该等于在 `lastNumber` 中缓存的数值.** 只有确保了这个不变性条件不被破坏上面的 Servlet 才是正确的。当在不变性条件中涉及多个变量时，各个变量之间并不是彼此独立的，而是某个变量的值会对其他变量的值产生约束。因此，当更新某一个变量时，需要在同一个原子操作中对其他变量同时进行更新。
在某些执行时序中, `UnsafeCachingFactorizer` 可能会破坏这个不变性条件。在使用原子引用的情况下，尽管对 set 方法的每次调用都是原子的，但仍然无法同时更新 lastNumber 和lastFactors。如果只修改了其中一个变量，那么在这两次修改操作之间，其他线程将发现不变性条件被破坏了。同样，我们也不能保证会同时获取两个值:在A 取这两个值的过中线程 B可能修改了它们，这样线程A也会发现不变性条件被破坏了。

#### 2.3.1 内置锁
Java 提供了一种内置的锁机制来支持原子性: **同步代码块 (Synchronized Block)** (第3章将介绍加锁机制以及其他同步机制的另一个重要方面:可见性)同步代码块包括两部分: **一个作为锁的对象引用, 一个作为由这个锁保护的代码块.** 以关键字 synchronized 来修饰的方法就是一种横跨整个方法体的同步代码块，其中该同步代码块的锁就是方法调用所在的对象。静态的synchronized 方法以Class 对象作为锁:
```java
synchronized (lock) {
    // 访问或修改由锁保护的共享状态
}
```
每个Java 对象都可以用做一个实现同步的锁, 这些锁被称为`内置锁(Intrinsic Lock) 或监视器锁(Monitor Lock)`。线程在进入同步代码块之前会自动获得锁，并且在退出同步代码块时自动释放锁，而 **无论是通过正常的控制路径退出，还是通过从代码块中抛出异常退出, 获得内置锁的唯一途径就是进入由这个锁保护的同步代码块或方法**。Java 的内置锁相当于一种互斥体(或互斥锁), 这意味着最多只有一个线程能持有这种锁, 当线程A尝试获取一个由线程 B 持有的锁时, 线程A 必须等待或者阻塞, 直到线程 B 释放这个锁。如果 B 永远不释放锁, 那么 A 也将永远地等下去。

**由于每次只能有一个线程执行内置锁保护的代码块，因此，由这个锁保护的同步代码块会以原子方式执行，多个线程在执行该代码块时也不会相互干扰。** 并发环境中的原子性与事务应用程序中的原子性有着相同的含义--一组语句作为一个不可分割的单元被执行。任何一个执行同步代码块的线程，都不可能看到有其他线程正在执行由同一个锁保护的同步代码块。这种同步机制使得要确保因数分解 Servlet 的线安全性变得更简单。在序清单 2-6 中使用了关键字 synchronized 来修饰 service 方法，因此**在同一时刻只有一个线程可以执行 service方法**。现在的 `SynchronizedFactorizer` 是线程安全的。然而，这种方法却过于极端，因为多个客户端无法同时使用因数分解 Servlet，服务的响应性非常低，无法令人接受。**这是一个性能问题**, 而不是线程安全问题, 我们将在 2.5 节解决这个问题。

#### 2.3.2 重入
当某个线程请求一个由其他线程持有的锁时，发出请求的线程就会阻塞。然而，由于内置锁是可重人的，**因此如果某个线程试图获得一个已经由它自己持有的锁，那么这个请求就会成功。“重入”意味着获取锁的操作的粒度是“线程”，而不是“调用”。** 重入的一种实现方法是, 为每个锁关联一个获取计数值和一个所有者线程。当计数值为0时，这个锁就被认为是没有被任何线程持有。当线程请求一个未被持有的锁时，JVM 将记下锁的持有者，并且将获取计数值置为 1。如果同一个线程再次获取这个锁，计数值将递增，而当线程退出同步代码块时计数器会相应地递减。当计数值为 0时, 这个锁将被释放。

重入进一步提升了加锁行为的封装性，因此简化了面向对象并发代码的开发。在程序清单 2-7 的代码中，子类改写了父类的 synchronized 方法，然后调用父类中的方法，此时**如果没有可重入的锁，那么这段代码将产生死锁**。由于 Widget 和LoggingWidget 中 doSomething 方法都是synchronized方法，因此每个 doSomething 方法在执行前都会获取 Widget 上的锁。然而，如果内置锁不是可重入的，那么在调用 `super.doSomething` 时将无法获得 Widget 上的锁，因为这个锁已经被持有，从而线程将永远停顿下去，等待一个永远也无法获得的锁。重入则避免了这种死锁情况的发生。
```java
// 程序清单 2-7 如果内置锁不是可重入的，那么这段代码将发生死锁
public class Widget{ 
    public synchronized void doSomething() {
        // ...
    }
}

public class LoggingWidget extends Widget {
    public synchronized void doSomething();
    System.out.println(toString() +"; calling doSomething");
    super.doSomething();
}
```

### 2.4 用锁来保护状态
**由于锁能使其保护的代码路径以串行形式来访问, 因此可以通过锁来构造一些协议以实现对共享状态的独占访问. 只要始终遵循这些协议，就能确保状态的一致性。** 访问共享状态的复合操作，例如命中计数器的递增操作(读取 -修改 - 写或者延迟初始化(先检查后执行))，都必须是原子操作以避免产生竞条件。如果在复合操作的执行过程中持有一个锁，那么会使复合操作成为原子操作。然而，仅仅将复合操作封装到一个同步代码块中是不够的。如果用同步来协调对某个变量的访问，那么在访问这个变量的所有位置上都需要使用同步。而且，当使用锁来协调对某个变量的访问时，在访问变量的所有位置上都要使用同一个锁。

**对象的内置锁与其状态之间没有内在的关联**。虽然大多数类都将内置锁用做一种有效的加锁机制，但**对象的域并不一定要通过内置锁来保护: 当获取与对象关联的锁时，并不能阻止其他线程访问该对象，某个线程在获得对象的锁之后，只能阻止其他线程获得同一个锁。之所以每个对象都有一个内置锁，只是为了免去显式地创建锁对象。** 你需要自行构造加锁协议或者同步策略来实现对共享状态的安全访问，并且在程序中自始至终地使用它们。

一种常见的加锁约定是，将所有的可变状态都封装在对象内部，并通过对象的内置锁对所有访问可变状态的代码路径进行同步，使得在该对象上不会发生并发访问。在许多线程安全中都使用了这种模式，例如 Vector 和其他的同步集合类。在这种情况下，对象状态中的所有变量都由对象的内置锁保护起来. **当某个变量由锁来保护时，意味着在每次访问这个变量时都需要首先获得锁，这样就确保在同一时刻只有一个线程可以访问这个变量。** 当类的不变性条件涉及多个状态变量时，那么还有另外一个需求: 在不变性条件中的每个变量都必须由**同一个锁**来保护。因此可以在单个原子操作中访问或更新这些变量，从而确保不变性条件不被破坏。在 SynchronizedFactorizer 类中说明了这条规则: 缓存的数值和因数分解结果都由 Servlet 对象的内置锁来保护

> 对于每个包含多个变量的不变性条件，其中涉及的所有变量都需要由同一个锁来保护。

如果同步可以避免竞态条件问题，那么为什么不在每个方法声明时都使用关键字synchronized ?事实上，如果不加区别地滥用 synchronized，可能导致程序中出现过多的同步. 此外，**如果只是将每个方法都作为同步方法，例如 Vector，那么并不足以确保 Vector 上复合操作都是原子的:**
```java
if (!vector.contains(element))
    vector.add(element);
```

虽然 contains 和add 等方法都是原子方法，但在上面这个“**如果不存在则添加(put-if-absent)**”的操作中仍然存在竞态条件。虽然 synchronized 方法可以确保单个操作的原子性，但如果要把多个操作合并为一个复合操作，还是需要额外的加锁机制(请参见4.4节了解如何在线程安全对象中添加原子操作的方法)。此外，将每个方法都作为同步方法还可能导致活跃性问题(Liveness)或性能问题 (Performance)，我们在SychronizedFactorizer 中已经看到了这些问题

### 2.5 活跃性与性能
在 `UnsafeCachingFactorizer` 中，我们通过在因数分解 Servlet 中引入了缓存机制来提升性能。在缓存中需要使用共享状态，因此需要通过同步来维护状态的完整性。然而，如果使用 SynchronizedFactorizer 中的同步方式，那么代码的执行性能将非常糟糕。SynchronizedFactorizer 中采用的同步策略是，通过 Servlet 对象的内置锁来保护每一个状态变量，该策略的实现方式也就是对整个 service 方法进行同步。虽然这种简单且粗粒度的方法能确保线程安全性，但付出的代价却很高。

**由于service是一个synchronized 方法，因此每次只有一个线程可以执行。这就背离了Serlvet 框架的初衷**，即 Serlvet 需要能同时处理多个请求，这在负载过高的情况下将给用户带来糟糕的体验。如果 Servlet 在对某个大数值进行因数分解时需要很长的执行时间，那么其他的客户端必须一直等待，直到 Servlet 处理完当前的请求，才能开始另一个新的因数分解运算。如果在系统中有多个 CPU 系统，那么当负载很高时，仍然会有处理器处于空闲状态。即使一些执行时间很短的请求，比如访问缓存的值，仍然需要很长时间，因为这些请求都必须等待前一个请求执行完成。

程序清单2-8 中的 CachedFactorizer 将 Servlet 的代码修改为**使用两个独立的同步代码块，每个同步代码块都只包含一小段代码**。其中一个同步代码块负责保护判断是否只需返回缓存结果的“先检查后执行”操作序列，另一个同步代码块则负责确保对缓存的数值和因数分解结果进行同步更新。此外，我们还重新引人了“命中计数器”，添加了一个“缓存命中”计数器，并在第一个同步代码块中更新这两个变量。由于这两个计数器也是共享可变状态的一部分，因此必须在所有访问它们的位置上都使用同步。位于同步代码块之外的代码将以独占方式来访问局部(位于栈上的)变量，这些变量不会在多个线程间共享，因此不需要同步。
```java
// 程序清单2-8缓存最近执行因数分解的数值及其计算结果的 Servlet
@Threadsafe
public class CachedFactorizer implements Servlet{
    @GuardedBy("this") private BigInteger lastNumber;
    @GuardedBy("this") private BigInteger[] lastFactors;
    @GuardedBy("this") private long hits;
    @GuardedBy("this") private long cacheHits;

    public synchronized long getHits(){
        return hits;
    }
    
    public synchronized double getCacheHitRatio() {
        return (double) cacheHits / (double) hits;
    }

    public void service(ServletRequest reg， ServletResponse resp){
        BigInteger i = extractFromRequest(reg);
        BigInteger factors = null;
        synchronized (this){
            ++hits;
            if(i.equals(lastNumber))
                ++cacheHits;
        }
        factors = lastFactors.clone();
        if(factors == nul1)
            factors = factor(i);
        synchronized (this){
            lastNumber = i;
            lastFactors = factors.clone();
        }
        encodeIntoResponse(resp, factors);
    }
}
```

在 CachedFactorizer 中不再使用 AtomicLong 类型的命中计数器，而是使用了一个long类型的变量。当然也可以使用AtomicLong 类型，但使用CountingFactorizer 带来的好处更多。对在单个变量上实现原子操作来说，原子变量是很有用的，但由于我们已经使用了同步代码块来构造原子操作，而使用两种不同的同步机制不仅会带来混乱，也不会在性能或安全性上带来任何好处，因此在这里不使用原子变量。

重新构造后的 CachedFactorizer 实现了在简单性(对整个方法进行同步)与并发性(对尽可能短的代码路径进行同步)之间的平衡。在获取与释放锁等操作上都需要一定的开销，因此如果将同步代码块分解得过细(例如将 +hits 分解到它自己的同步代码块中)，那么通常并不好，尽管这样做不会破坏原子性。当访问状态变量或者在复合操作的执行期间，CachedFactorizer 需要持有锁，但在执行时间较长的因数分解运算之前要释放锁。这样既确保了线程安全性，也不会过多地影响并发性，而且在每个同步代码块中的代码路径都“足够短”.


## 代码附录
以下依据书本例子编写了实际简单可执行的代码
### 1. `StatelessFactor`
```java
public class StatelessFactor {
    public void service(Integer i){
        Integer[] fs = factor(i);
        System.out.println(Arrays.toString(fs));    // 模拟 response
    }

    public static void main(String[] args) {
        StatelessFactor f = new StatelessFactor();
        f.service(5);
    }
}
```
`factor` 代码:
```java
public class ArtificialUtils {
    public static Integer[] factor(Integer i){
//        System.out.println(">>>> "+Thread.currentThread().getName()+" enter factor()-"+i);
        Integer[] a = {1, 2, 3};
//        System.out.println(">>>> "+Thread.currentThread().getName()+" finish factor()-"+i);
        return a;
    }
}
```

### 2. `UnsafeCountingFactor`
```java
public class UnsafeCountingFactor {
    private long count = 0;
    public long getCount(){
        return count;
    }
    public void service(Integer i){
        Integer[] fs = factor(i);
        count++;    // 每被调用一次, 计数值加一
//        System.out.println(Arrays.toString(fs));
    }
    public static void main(String[] args) throws InterruptedException {
        UnsafeCountingFactor f = new UnsafeCountingFactor();
        int tn = 10000;     // 此时更可能发生数据不一致的情况
        Thread[] threads = new Thread[tn];
        for(int i=0; i<tn; i++)
            threads[i] = new Thread(()-> f.service(5));
        for(int i=0; i<tn; i++)
            threads[i].start();
        for(int i=0; i<tn; i++)
            threads[i].join();
        System.out.println(f.getCount()==tn);   // 大概率是 false

    }
}
```

### 3. `CachedFactorizer`
> 它首先是可以正确工作的, 其次它也较好的考虑到了性能: 这通过引入一个本地变量实现 -- 本地变量 是不共享的.
> 在正确性上, 程序引入 this 锁 首先保护了 hits cacheHits 的同步更新, 同时同步读取了 lastNumber 和 lastFactors
> 这里是指判断 i 等于 lastNumber 并把 lastFactors 复制到 factors 的过程, 这保证了 i 和 factors 必然是一致的.
> 随后, 可以使用 factors 进行条件判断并决定是否进行 factor 这一耗时操作(如果进行, 其结果最后保存在 本地变量 factors中)
> 最后, 我们再使用同一个 this 锁来同步更新 lastNumber 和 lastFactors.

```java
public class CachedFactor {
    // 该类具有四个可变域(状态), 需要统一一把锁进行管理
    private Integer lastNumber;
    private Integer[] lastFactors;
    private long hits;
    private long cacheHits;
    /** 返回服务调用次数 */
    public synchronized long getHits(){
        return hits;
    }
    /** 计算命中率 */
    public synchronized double getCacheHitRatio(){
        return (double)cacheHits / (double)hits;
    }
    /** 服务程序 */
    public void service(Integer i){
        Integer[] factors = null;
        // 1. 该同步块判断是否命中, 并将 lastFactors 复制到 本地变量
        synchronized (this) {
            hits++;
            if (i.equals(lastNumber)) {
                cacheHits++;
                factors = lastFactors.clone();
            }
        }
        // 2. 在非同步的情况下, 可以使用本地变量进行判断和计算等操作
        if (factors == null)
            factors = factor(i);    // 此为耗时操作, 尽量并发进行(也即不要将它放在同步代码块内)
        // 3. 接下来的同步块更新 lastNumber 和 lastFactors
        synchronized (this){
            lastNumber = i;
            lastFactors = factors.clone();
        }
//        System.out.println(Arrays.toString(factors));   // 模拟封装响应
    }
    /** 测试代码 */
    public static void main(String[] args) throws InterruptedException {
        CachedFactor f = new CachedFactor();
        int tn = 1000;
        Thread[] threads = new Thread[tn];
        for(int i=0; i<tn; i++) {
            Integer j = (int) (Math.random()*10);   // 0-9 的数
            threads[i] = new Thread(() -> f.service(j));
        }
        for(int i=0; i<tn; i++)
            threads[i].start();
        for(int i=0; i<tn; i++)
            threads[i].join();
        // 查看缓存命中率
        System.out.println(f.getHits());
        System.out.println(f.getCacheHitRatio());
    }
}
```
