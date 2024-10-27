from graphviz import Digraph

dot = Digraph(comment='Atomic Ticket Purchase Transaction')

dot.node('A', 'Begin Transaction')
dot.node('B', 'Check Seat Availability')
dot.node('C', 'Reserve Seat')
dot.node('D', 'Process Payment')
dot.node('E', 'Confirm Purchase')
dot.node('F', 'Commit Transaction')
dot.node('G', 'Rollback Transaction')
dot.node('H', 'End Transaction')

# Successful transaction flow
dot.edges(['AB', 'BC', 'CD', 'DE', 'EF', 'FH'])

# Failure at payment processing
dot.edge('D', 'G', label='Payment Failure')
dot.edge('G', 'H')

dot.render('atomic_ticket_purchase_transaction.gv', view=True)
