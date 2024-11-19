# TicketingSystem
Thesis proposal, comparison of MySQL and MongoDB


2	Methodology
2.1	Research design
The study employed an experimental comparative methodology examinated MySQL and MongoDB implementations in a ticketing system through:
Controlled test scenarios
•	Basic concurrent booking operations
•	Schema management approaches
•	Transaction handling methods
Comparison framework
•	Implementation differences
•	Data handling approaches
•	Transaction management strategies
2.2	System requirements 
2.2.1	Technical infrastructure
Following Raab’s (2019) definition, the System Under Test encompassed the complete system infrastructure:
Raab, F. (2019). System Under Test. In: Sakr, S., Zomaya, A.Y. (eds) Encyclopedia of Big Data Technologies. Springer, Cham. https://doi.org/10.1007/978-3-319-77525-8_124
Hardware Specifications:
•	CPU (Specify CPU model and cores)
•	RAM (Specify RAM capacity and speed)
•	Storage (Specify storage type and capacity)
•	Network (Specify network configuration)
Software environment
•	Operating System (Specify OS and version
•	MySQL Version (Specify version)
•	MongoDB version (specify version)
•	Test framework (Junit/TestNG for unit tests)
•	Loading testing tool: Apache JMeter
Connection Pool configuration
•	Initial Size:10 
•	Maximum Size: 50
•	Timeout: 30 seconds
2.2.2	System architecture
Core Components:
1. Data Access Layer
   - DAO interfaces
   - MySQL/MongoDB implementations
   - Transaction management
2. Service Layer
   - BookingService
   - TicketService
   - UserService
3. Simulation Framework
   - ConcurrencySimulation
   - LoadTesting
   - MetricsCollection
2.3	Testing framework
2.3.1	Test environment configuration
Database Configurations:
- Connection Pool Settings:
  - Initial size: 10
  - Maximum size: 50
  - Timeout: 30 seconds

Test Parameters:
- Concurrent Users: 1000
- Max Tickets/User: 2
- Thread Pool: 10
- Duration: 1 minute
2.3.2	Test scenarios
2.3.2.1	Concurrent booking operations
Test Focus:
- Multiple users booking simultaneously
- Race condition handling
- Transaction isolation
- Data consistency

Metrics Collected:
- Success/failure rates
- Response times
- Data consistency 
2.3.2.2	Schema flexibility
Test Focus:
- Dynamic ticket categories
- Price rule modifications
- Attribute updates
- Query performance

Metrics Collected:
- Update times
- Query response times
- Storage efficiency
2.3.2.3	Transaction management
Test Focus:
- ACID compliance
- Rollback handling
- Partial failure recovery
- Consistency maintenance

Metrics Collected:
- Transaction completion rates
- Rollback frequency
- Data consistency checks
2.3.2.4	Schema modification 
Test Focus:
- Live schema updates
- Impact on operations
- System availability
- Performance degradation

Metrics Collected:
- Downtime duration
- Operation failures
- Response time impact
2.3.3	Data collection method
Performance metrics

Reliability metrics
2.4	Validation 

