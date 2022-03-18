## Lucene Monitor
- have put one test for non-persistence monitor.
- have added one test for persistence monitors -  you can configure a path for QuerIndex where all your queries will get indexed. So you can directly create a 
monitor object with the monitorConfiguration object which will have the path of your queryIndex and it takes all the queries from the index and you don't need
to register it again.
