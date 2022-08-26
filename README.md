# GDiff

We propose GDiff, to the best of our knowledge the first automated approach to conduct differential performance testing of GDBs.

# Getting Started

Requirements:
* Java 11
* [Maven](https://maven.apache.org/)
* The graph database systems that you want to test (now supporting Neo4j, RedisGraph, and Memgraph)

# Project Structure

* src
  * gdsmith
    * common: infrastructure
    * cypher: the openCypher AST
    * support for different databases[Neo4j, RedisGraph, ...]
* out: the executable jar file GDsmith.jar

# Using GDiff

Generally GDiff can be configured and executed using the following command:

```bash
java -jar GDsmith.jar [database] --[database_option1] --[database_option2] ...
```

Here are some examples of database options:

```
--port 7687 			//the port of the database driver is 7687
--username xxx			//the user of the database is "xxx"
--password ###			//the password of the user is "####"
--oracle ORACLE_NAME	//use the oracle "ORACLE_NAME"
```

Note that GDiff will not automatically create database user, as a result, you might need to manually create a user and grant it with the privilege for remote connection, executing queries, writing to databases and creating/deleting new databases.

Here are the set of databases supported by GDiff:

```
neo4j
redisgraph
memgraph
```

You can use these database names ([database]) to indicate which database you want to test.

```bash
java -cp GDsmith.jar gdsmith.Main --num-threads 1 composite --config [the relative path to the configuration file] --oracle MCTS
```

The configuration file in written in json form and is similar to the database_options, the difference is that you cannot specify an oracle for them.

```json
{
  "neo4j@4.4.10": {
    "port": 7687,
    "host": "localhost",
    "username": "neo4j",
    "password": "gdiff"
  },
  "redisgraph@2.10.1": {
    "port": 6379,
    "host": "localhost",
    "username": "redisgraph",
    "password": "gdiff"
  }
}
```

# Overview of GDiff IR

GDiff generate IR programs and then translate them to cypher queries. This section presents the semantics of the IR.

Here we assume that an IR interpreter is used, it takes in a schema, a database, an IR program and output the result of the IR program.

In order to understand the GDiff IR, we explain it from two perspectives. The first is by using it to write queries like Cypher, the second is the way we use it to generate safe IR which can be translated into cypher queries and these queries should not cause failures or undefined behaviors according to the cypher standard.



## Using GDiff IR to Write Queries

an IR program is a sequence of operators. The execution of the IR program is the process of the control point moving from the first operator to the last one. For each operator, it takes in the state generated passed from the former generator, changes the state and passes the new state to the next generator.

Here, "state" stands for data, and the data is organized as variables. For the GDiff IR, a variable is a data vector. Next, we will use examples for further explanation.



The match operator uses a pattern_tuple to match subgraphs in the database.

```
match(<pattern_tuple>, <where>)
```

 Here is an example:

```
match(node1-relation1->node2, node2.name=="x")
```

All the subgraphs in the databases which matches the pattern node1-relation1->node2 where node2 has a property "name" equals "x" will be extracted from the database and form a data vector which represents the pattern, suppose the result is like this:

```
Pattern1 == [(n1-r1->n2),(n1-r2->n3),(n2-r3->n4)]
```

And the variables are:

```
node1 == [n1, n1, n2]
node2 == [n2, n3, n4]
relation1 == [r1, r2, r3]
```

So the state passed to the next operator is:

```
state = {[(node1-relation1->node2)]} == {[(n1-r1->n2),(n1-r2->n3),(n2-r3->n4)]}
```

 Suppose the next operator is also a match operator:

```
match(node1-relation2->node3, node3.id==10)
```

For node1 is already defined, it will now be considered as a variable reference. hence, all the subgraphs in the databases which matches the pattern a-b->c->d (where "a" must be a node in vector node1, and node3 must have a property named id equals 10), suppose the result is like this:

```
Pattern2 == [(n1-r4->n5)]
```

And the state passed to the next operator should be:

```
state = {[node1-relation1->node2]x[node1-relation2->node3]} == Pattern1 x Pattern2 = {[(n1-r1->n2),(n1-r2->n3),(n2-r3->n4)] x [(n1-r4->n5)]} == {[(n1-r1->n2) x (n1-r4->n5), (n1-r2->n3) x (n1-r4->n5)]}
```

(For why the calculation works like this, please refer to the open-Cypher documentation)

So in a high level,  the IR operator is just used to transform a set of variables into a new set of variables, though its rule is complicated due to the semantic richness of open-Cypher.



## Using IR as a Tool to Model Cypher Semantics

Though the semantics of Cypher is complicated, we can use the GDiff IR to reduce the semantics that are not important for generating correct and undefined-behavior-free Cypher. We use a "local environment" to model the semantics of the IR, it has the following features:

​	Each operator has a local environment which is determined by all former operators.

​	Given the local environment and the present operator, the local environment of the next operator can be calculated.

​	A rule can be used to judge whether the operator satisfies the local environment, if all operators satisfy their own local environments, the IR program can be translated into a correct and undefined-behavior-free Cypher query.



The local environment is formed by:

​	A set of usable variables.

​	A set of facts can be used to calculate the type and property info of variables.



Here are examples of local environment transition:

```
match(n1[label1]-r1->n2[label2, label3], true)
before:
{
	variables: {}
	facts: {}
}
after:
{
	variables: {n1, r1, n2}
	facts: {
		label(n1)=={label1}, label(n2)=={label2, label3},
		type(n1)==node, type(n2)==node, type(r1)==relationship
	}
}
```

```
match(n1[label1]-r1->n2[label2, label3], true) match(n1[label4], true)
//for the second operator:
before:
{
	variables: {n1, r1, n2}
	facts: {label(n1)=={label1}, label(n2)=={label2, label3}}
}
after:
{
	variables: {n1, r1, n2}
	facts: {
		label(n1)=={label1, label4}, label(n2)=={label2, label3},
		type(n1)==node, type(n2)==node, type(r1)==relationship
	}
}
```

```
match(n1[label1]-r1->n2[label2, label3], true) with(a=n1.name, n1.id==5)
//for the second operator:
//suppose that we know from our schema that nodes with label1 has an integer property named "id" and a string property named "name"
before:
{
	variables: {n1, r1, n2}
	facts: {label(n1)=={label1}, label(n2)=={label2, label3}}
}
after:
{
	variables: {a}
	facts: {
		type(a)==string
	}
}
```



Here are the transitions for each operator:

```
match(pattern_tuple, where)
after.variables = before.variables union get_new_variables(pattern_tuple)
after.facts.label_facts = before.facts.label_facts union get_label_facts(pattern_tuple)
after.facts.type_facts = before.facts.type_facts union get_new_variables(pattern_tuple).map(v-> if v is node then return type(v)==node else return type(v)==relationship)


with(alias_def_tuple, order, skip, limit, where)
after.variables = get_new_variables(pattern_tuple)
after.facts.label_facts = (before.facts.label_facts union get_label_facts(alias_def_tuple)).filter(fact -> variable(fact) is still in after.variables)
after.facts.type_facts = (before.facts.type_facts union get_new_variables(pattern_tuple)).map(v-> return calculate_type(v.expr)).filter(fact -> variable(fact) is still in after.variables)

unwind(expr as v)
after.variables = before.variables union {v}
after.facts.label_facts = before.facts.label_facts
after.facts.type_facts = before.facts.type_facts union {type(v)==expr.type.list_element_type}

return(alias_def_tuple, order, skip, limit)
//same as with
after.variables = get_new_variables(pattern_tuple)
after.facts.label_facts = (before.facts.label_facts union get_label_facts(alias_def_tuple)).filter(fact -> variable(fact) is still in after.variables)
after.facts.type_facts = (before.facts.type_facts union get_new_variables(pattern_tuple)).map(v-> return calculate_type(v.expr)).filter(fact -> variable(fact) is still in after.variables)
```

