2	Methodology
2.1	Research design 
This study adopted a comparative experimental research approach (Reisner, 1988) (Sygayret et al., 2022), to examine how MySQL and MongoDB handle transactional behaviour, schema rigidity, and nested structures within a ticketing system context The experimental design allowed for a direct comparison of database performance under controlled conditions by systematically implementing and testing identical functionalities in both systems.
The selection of a comparative experimental methodology was based on prior studies ( C. Győrödi et al., 2015; Patil et al., 2017; Capris et al., 2022; Stonebraker & Pavlo, 2024), that demonstrated its effectiveness in highlighting performance differences between SQL and NoSQL databases. This approach enabled for the objective measurement of each database’s capabilities in managing concurrent transactions, maintaining data consistency, and handling data relationships.
The study is based on pragmatism, emphasizing practical outcomes and real-world applicability. By implementing a simulation rather than relying solely on theoretical analysis, the proposal aimed to produce findings that are directly relevant to practitioners facing similar database selection challenges. This aligned with the recommendations of Shareef, Sharif and Rashid (2022), who identified the need for studies that provide actionable insights into database performance in specific application scenarios.
The experimental framework encompassed three key areas of investigation:
i.	Transactional behaviour in synchronous ticket booking scenarios
ii.	Impact of schema design processes on system implementation
iii.	Management of nested data structures in booking records
The comparative analysis was structured through:
i.	Controlled test scenarios that examined basic booking operations, transaction handling methods, and schema management techniques
ii.	Systematic comparison of implementation differences, data handling strategies, and transaction management mechanisms
iii.	Quantitative measurement of performance metrics and qualitative assessment of development experiences
This methodological framework enabled systematic examination of how each database system addressed the core research objectives through controlled experimentation and structured comparison.
2.2	Technical infrastructure
All the experiments were implemented on a development workstation Windows 11 Home. The system utilized an Intel(R) Core (TM) i5-12500H Processor 12th Generation, 16gb ram, 2500Mhz and SSD for storage operations, 12 Core(s), 16 Logical Processor(s). Network connectivity was maintained through 500mb ethernet connection to minimize latency impacts on database operations
The software environment used MySQL Community Server 8.0. and MongoDB Community Service 8.0.3. Database management was assisted through MySQL Workbench 8.0.40 and MongoDB Compass 1.44.6 respectively. The development stack included OpenJDK 23.0.1 for core implementation, with Eclipse IDE 2023-09 serving as the primary development environment. The MongoDB shell, Mongosh 2.3.3, was used for direct interaction with the MongoDB database.
Maven 3.9.5 managed project dependencies and build automation. Version control was maintained through Git 2.42.0, with project artifacts stored in a private repository. Test data generation utilized Mockaroo’s for creating realistic user profiles. 
The system architecture separated concerns across:
•	Entity definitions
•	Data Access Objects (DAO)
•	Service layer
•	Test simulation framework
2.3	Analysis Method
2.3.1	Quantitative 
The method employed to evaluate the performance characteristics and behavioural patterns of MySQL and MongoDB was collected with instrumented service classes and testing scenarios. This proposal aligned with the methodology proposed by Osman and Knottenbelt (2012) for database performance evaluation. 
In terms of success rate, the formula utilized was:
Success Rate = (Successful Bookings / Total Booking Attempts) × 100
According to Bernstein & Newcomer (2009b) transaction success rate directly correlates with system reliability and user experience in OLTP (Online Transaction Processing) systems. For ticketing systems specifically, a success rate above 95% is considered industry standard.
Another common solution is to use an AtomicInteger addition operation to advance a global logical timestamp. This requires fewer instructions and thus the DBMS’s critical section is locked for a smaller period of time (Yu et al., 2014).
private final AtomicInteger successfulBookings = new AtomicInteger(0);
private final AtomicInteger failedBookings = new AtomicInteger(0);
Consequently, the average query time was taken in consideration to compare both databases performance. For ticketing systems, Zhao et al. (2020) recommend maintaining average query times below 100ms for optimal user experience.
Average Query Time (ms) = Total Query Time / Total Queries
This implementation followed the measurement methodology described by Cai et al. (2019), which emphasizes that timing metrics are more reliable than individual query measurements.
For the concurrency metrics, an evaluation of transaction effectiveness was performed based on Kleppmann (2017) who said conflict rates in distributed systems proved insight into the effectiveness of concurrency control mechanisms. 
Conflict Rate = (Number of Concurrency Conflicts / Total Transactions) × 100
The schema modification success rate indicated the adaptability of the database (Möller et al., 2020).
Modification Success Rate = (Successful Modifications / Total Modification Attempts) × 100
The formula for the optimal size of a thread pool was:
int optimalThreadPoolSize = numberOfCores * targetUtilization * (1 + waitTime / computeTime)
Metric for schema modification 
duration = (System.nanoTime() - startTime) / 1_000_000;
The application the mean and coefficient of variation (CV)(Kaltenecker et al., 2023) was utilized to analyse and compare the performance metrics of MySQL and MongoDB.
Mean:
Purpose: The mean is used to determine the average value of a dataset, providing a central value that summarizes the data.
Formula: Mean (x̅) = Sum of Values / Number of Values
Application: It helped in understanding the typical performance of each database across various metrics such as total time, query time, and schema modification time.
Coefficient of Variation (CV):
Purpose: The CV measured the relative variability of the data, expressed as a percentage. It allowed for the comparison of the degree of variation between datasets, regardless of their units or scales.
Coefficient of variation = σ/μ × 100%
Where 𝜎 is the standard deviation and 𝜇 is the mean.
Application: It was used to assess the consistency and stability of each database's performance by comparing the variability in metrics such as total time, query time, and schema modification time.
2.3.2	Qualitative
Following Lenberg et al.’s (2023) framework for qualitative software engineering research, this study employed an interpretative analysis to examine behavioural patterns and implementation challenges in database systems. The methodology emphasized reflexivity and documentation of development experiences, aligned with established qualitative research practices in software engineering (Liang et al., 2023). 
For validation, peer review sessions evaluated implementation approaches, ensuring adherence to best practices and design patterns, providing feedback on architectural decisions, and optimization opportunities.
The qualitative methodology complemented quantitative metrics by providing context for performance variations and identifying underlying causes of observed behavioural differences between both databases implementations.
2.4	Research validation strategy
2.4.1	Architectural overview
The architecture employed a layered design with three primary components: Data Access Layer, Service Layer, and Simulation Framework. Each layer served specific validation objectives while maintaining separation of concerns (Ingeno, 2018).
i.	Data Access Layer
The data access layer established the foundation, providing consistent interfaces for database operations while isolating database-specific implementations. This procedure ensured that differences in performance and behaviour could be attributed to the underlying databases rather than implementation variations.
ii.	Service Layer
The service layer managed business logic and transaction coordination, implementing distinct strategies appropriate to each database’s capabilities. MySQL implementation used JPA/Hibernate’s transaction management with pessimistic locking to ensure data consistency under concurrent access. In contrast, MongoDB implementation utilized Morphia’s object-document mapping with optimistic concurrency control, reflecting the different approaches to transaction management. These strategies enabled evaluation of the first hypothesis regarding concurrency handling
iii.	Simulation Framework
This framework managed parallel booking operations through configured thread pools, allowing systematic testing of concurrent access patterns. It also incorporated metrics collection, tracking response times, and transaction resource utilization. These measurements provided quantitative data for evaluating the second hypothesis regarding query performance.
2.4.2	Validation Support
The architecture accommodated both relational and document-base data models while maintain functional equivalence to ensure a fair comparison. Core domain entities, including Events, Tickets, Booking, and Users, were implemented to preserve essential functionalities. This technique helped to evaluate the third hypothesis regarding schema flexibility by allowing runtime modifications while measuring their impact on system performance.
MySQL followed traditional relational modelling practices, using normalized tables with foreign key constraints to maintain referential integrity (https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-relational-modeling.html). MongoDB adopted a document-oriented process utilizing embedding and referencing strategies appropriate to the data access patterns (https://www.mongodb.com/docs/manual/core/schema-validation/). 
Initial tests established baseline performance metrics for both implementations under normal operating conditions. Subsequent tests introduced controlled stress conditions, including parallel booking attempts and runtime schema modifications. The metrics collected provided evidence for evaluating the hypotheses while the controlled environment ensured reproducibility of results. 
2.5	Testing Methodology
2.5.1	Performance Test Scenarios
The performance testing focused primarily on quantitative measurement of database behaviour under controlled conditions. Specifically, test scenarios were structured to evaluate system performance across multiple dimensions.
In the first phase, transaction response time analysis provided baseline performance data. Specifically, measured query execution duration across various operation types. Additionally, transaction competition rates underwent continuous monitoring to assess system throughput under different load conditions. 
2.5.2	Concurrency Tests cases
Following the performance evaluation, the methodology examined database behaviour under simultaneous access patterns, focusing on transaction isolation and resource contention handling under conditions similar to production environments.
During testing, the study concentrated on each database’s handling of concurrent transactions, observing transaction processing and resource allocation. 
In addition to performance metrics, data consistency formed a central component of concurrency testing. Under these circumstances, each system showcased unique characteristics. 
2.5.3	Schema Modification tests
The final phase focused on schema modification testing to assess structural adaptability. Initially, draw from Sadalage & Fowler (2012) work on schema evolution patterns, the testing progressed through increasing levels of complexity.
Basic schema alterations, such as column additions and modifications were performed to evaluate each database’s ability to maintain data integrity during structural changes while handling concurrent transactions.
Performance metrics were collected during schema modifications to assess impact on system availability and response times (Gallinucci et al., 2018). The evaluation focused on the ability to maintain transaction processing capabilities during structural changes. 
2.5.4	Data and Process validation
Ensuring data integrity is fundamental for the reliable operation of any system, since it plays a central role in detecting and correcting errors, inconsistences, and inaccuracies within datasets. The primary types of data validation employed were format, and consistency validation.
Building upon the methodology proposed by Van Der Loo & De Jonge (2020)validation was meet as a surjective function mapping datasets to Boolean values. This method implanted through explicit validation rules in both MySQL and MongoDB databases, allowing for detection of data anomalies based on specific requirements:
i.	Single-Point Validations: Focused on individual data points, such as checking the status of a ticket.
ii.	Cross-Variable Validations: Examined relationships between different fields within a record to ensure logical consistency.
iii.	Cross-Record Validations: Assessed constraints across multiple records, for instance, booking limitations affecting several tickets.
iv.	Temporal Validations: Monitored changes in data over time, tracking the evolution of ticket statuses.
Both databases employed different procedures to enforce consistency. MySQL maintained consistency through atomic transactions using EntityManager, along with the application of pessimistic locking for concurrent access control. Data integrity constraints were also utilized including foreign key relationships to maintain referential integrity, unique constraints to prevent duplicate bookings, and check constraints to validate business rules.
In contrast, MongoDB application utilized SessionFactory to manage multi-document transactions that ensured atomicity across multiple collections. Optimistic concurrency control was implemented (in contrast to MySQL pessimistic locking) using version fields within documents. Finally, schema validation was enforced through JSON schema definitions, specifying structure and data types for documents. 
Validating concurrent operations was important to address the challenges posed by simultaneous access and modification of data by multiple users. Key considerations included handling race conditions, preventing deadlocks, and maintained consistency levels.
The schema validation process ensured that changes to the database did not disrupt ongoing operations or compromise data integrity. Conversely, MongoDB benefited from its flexible schema design, allowing dynamic updates without adversely affecting existing documents.

