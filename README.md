## Relish
Relish is a prototype for relational program synthesis.

### Publication

- Yuepeng Wang, Xinyu Wang, Isil Dillig.
  Relational Program Synthesis. OOPSLA 2018

### Dependencies

- CMBC 5.11

### Build

Relish can be built using Ant
```
ant jar
```

### Usage

You can run Relish on an encoder/decoder benchmark
```
./run-codec.sh <benchmark-file-path> <result-file-path>
```
or a comparator benchmark
```
./run-comparator.sh <benchmark-file-path> <result-file-path>
```

For example, you may want to try the following examples in the root directory of this repo
```
./run-codec.sh exp/benchmark/codec/b1 b1.log
```
or
```
./run-comparator.sh exp/benchmark/comparator/c1 c1.log
```

### Tests
The tests can be built and executed with Ant
```
ant test
```

