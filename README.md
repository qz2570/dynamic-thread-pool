# Description
This is a lightweight Spring Boot middleware that implements real-time monitoring and dynamic adjustment of thread pool configurations.
# What's dynamic thread pool?
Dynamic thread pool allows real-time change of thread pool parameters without redeploying your application. It allows developers adjust and validate the parameters' reasonableness quickly based on its dynamic change capabilities.
# Why dynamic thread pool?
While thread pools bring performance and throughput improvements to our application, they also come with many risks and issues. The primary reason is that it is difficult to configure reasonable thread pool parameters. 
The difficulty in configuring thread pool parameters reasonably forces us to focus on the following three pain points:
## Hard to monitor the thread pool state: 
Developers usually find it hard to know how many threads have been created in each thread pool, whether there are backlogged tasks in the queue, what's the currently state of thread pool, or whether the thread pool has been exhausted... These problems could often only be awared after issues arise in product or receiving customer complaints.
## Hard to locate thread rejection: 
Even if we quickly detect a rejection occurs, it is often difficult to get the thread stack info at the time of the issue due to the short duration of the rejection. As a result, it is usually hard or even impossible to quickly identify the cause of the rejection. For example, whether a sudden surge in traffic filled up the thread pool, or if a particular business logic took too long and held up the threads in the pool.
## Hard to address parameters issues: 
Once we identify that a thread pool's parameter configuration is unreasonable, we need to make adjustments accordingly. However, the time taken for the `modify -> package -> approval -> release` process can potentially increase the severity of the incident. Additionally, due to the difficulty in setting reasonable thread pool parameters, we may have to repeatedly go through the `modify -> package -> approval -> release' cycle.`
