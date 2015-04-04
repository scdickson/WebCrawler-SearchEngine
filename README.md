CS390 Web Crawler and Search Engine
==========

A multithreaded web crawler and search engine. A pool of threads is used to crawl a specified domain
using depth first search. Valid links and words on each page are added to a MySQL database. Results
are searchable using the companion search engine. Additional features such as a graph of the subdomains
and connections between them are available.


Syntax for crawler:
java Crawler [-u maxurls] [-d domain] url-list 


Library dependencies are available in the "Libraries" folder.


![Screen 1](https://raw.githubusercontent.com/scdickson/WebCrawler-SearchEngine/master/Search1.png)
![Screen 2](https://raw.githubusercontent.com/scdickson/WebCrawler-SearchEngine/master/Search2.png)
![Screen 3](https://raw.githubusercontent.com/scdickson/WebCrawler-SearchEngine/master/SubdomainGraph.jpg)
![Screen 4](https://raw.githubusercontent.com/scdickson/WebCrawler-SearchEngine/master/Search3.png)
