Much has been already covered by the main **dlock** README file.
And here is the main implementation of **dlock** library.
JDBC API is the main focus of the **dlock** library.
However, _dlock-api_ and _dlock-core_ stay open for any other, repository-based concretization. 
As long as the chosen engine (e.g. Cache or NOSQL) gives you 100% guarantee of safety. 

Also, don't forget to run jhm tests with the following command to see it in action
```shell script
\> gradlew jmh
```

Here is some raw example of my own tests run on my private laptop (Dell XPS 9560)

```text
REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                                                                                               Mode  Cnt       Score   Error   Units
KeyLockAndReleaseNoCollisionH2Benchmark.tryAndReleaseLockNoCollision                                   thrpt            5,182          ops/ms
KeyLockAndReleaseNoCollisionH2Benchmark.tryAndReleaseLockNoCollision:Ěgc.alloc.rate                    thrpt           35,725          MB/sec
KeyLockAndReleaseNoCollisionH2Benchmark.tryAndReleaseLockNoCollision:Ěgc.alloc.rate.norm               thrpt        36536,069            B/op
KeyLockAndReleaseNoCollisionH2Benchmark.tryAndReleaseLockNoCollision:Ěgc.churn.G1_Eden_Space           thrpt          231,197          MB/sec
KeyLockAndReleaseNoCollisionH2Benchmark.tryAndReleaseLockNoCollision:Ěgc.churn.G1_Eden_Space.norm      thrpt       236448,171            B/op
KeyLockAndReleaseNoCollisionH2Benchmark.tryAndReleaseLockNoCollision:Ěgc.churn.G1_Old_Gen              thrpt            0,084          MB/sec
KeyLockAndReleaseNoCollisionH2Benchmark.tryAndReleaseLockNoCollision:Ěgc.churn.G1_Old_Gen.norm         thrpt           85,629            B/op
KeyLockAndReleaseNoCollisionH2Benchmark.tryAndReleaseLockNoCollision:Ěgc.churn.G1_Survivor_Space       thrpt            0,594          MB/sec
KeyLockAndReleaseNoCollisionH2Benchmark.tryAndReleaseLockNoCollision:Ěgc.churn.G1_Survivor_Space.norm  thrpt          607,108            B/op
KeyLockAndReleaseNoCollisionH2Benchmark.tryAndReleaseLockNoCollision:Ěgc.count                         thrpt           40,000          counts
KeyLockAndReleaseNoCollisionH2Benchmark.tryAndReleaseLockNoCollision:Ěgc.time                          thrpt          304,000              ms
KeyLockCollisionH2Benchmark.tryLockAlwaysCollision                                                     thrpt           32,123          ops/ms
KeyLockCollisionH2Benchmark.tryLockAlwaysCollision:Ěgc.alloc.rate                                      thrpt           87,445          MB/sec
KeyLockCollisionH2Benchmark.tryLockAlwaysCollision:Ěgc.alloc.rate.norm                                 thrpt         3294,103            B/op
KeyLockCollisionH2Benchmark.tryLockAlwaysCollision:Ěgc.churn.G1_Eden_Space                             thrpt          115,692          MB/sec
KeyLockCollisionH2Benchmark.tryLockAlwaysCollision:Ěgc.churn.G1_Eden_Space.norm                        thrpt         4358,212            B/op
KeyLockCollisionH2Benchmark.tryLockAlwaysCollision:Ěgc.churn.G1_Old_Gen                                thrpt            0,025          MB/sec
KeyLockCollisionH2Benchmark.tryLockAlwaysCollision:Ěgc.churn.G1_Old_Gen.norm                           thrpt            0,929            B/op
KeyLockCollisionH2Benchmark.tryLockAlwaysCollision:Ěgc.churn.G1_Survivor_Space                         thrpt            0,347          MB/sec
KeyLockCollisionH2Benchmark.tryLockAlwaysCollision:Ěgc.churn.G1_Survivor_Space.norm                    thrpt           13,058            B/op
KeyLockCollisionH2Benchmark.tryLockAlwaysCollision:Ěgc.count                                           thrpt            5,000          counts
KeyLockCollisionH2Benchmark.tryLockAlwaysCollision:Ěgc.time                                            thrpt           17,000              ms
KeyLockNoCollisionH2Benchmark.tryLockNoCollision                                                       thrpt            6,931          ops/ms
KeyLockNoCollisionH2Benchmark.tryLockNoCollision:Ěgc.alloc.rate                                        thrpt           38,352          MB/sec
KeyLockNoCollisionH2Benchmark.tryLockNoCollision:Ěgc.alloc.rate.norm                                   thrpt         6703,479            B/op
KeyLockNoCollisionH2Benchmark.tryLockNoCollision:Ěgc.churn.G1_Eden_Space                               thrpt          413,581          MB/sec
KeyLockNoCollisionH2Benchmark.tryLockNoCollision:Ěgc.churn.G1_Eden_Space.norm                          thrpt        72289,007            B/op
KeyLockNoCollisionH2Benchmark.tryLockNoCollision:Ěgc.churn.G1_Old_Gen                                  thrpt            0,235          MB/sec
KeyLockNoCollisionH2Benchmark.tryLockNoCollision:Ěgc.churn.G1_Old_Gen.norm                             thrpt           41,032            B/op
KeyLockNoCollisionH2Benchmark.tryLockNoCollision:Ěgc.churn.G1_Survivor_Space                           thrpt            0,605          MB/sec
KeyLockNoCollisionH2Benchmark.tryLockNoCollision:Ěgc.churn.G1_Survivor_Space.norm                      thrpt          105,796            B/op
KeyLockNoCollisionH2Benchmark.tryLockNoCollision:Ěgc.count                                             thrpt           19,000          counts
KeyLockNoCollisionH2Benchmark.tryLockNoCollision:Ěgc.time                                              thrpt          177,000              ms
```