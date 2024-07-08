import sqlite3
import csv

# Connect to the SQLite database
conn = sqlite3.connect('./TrendyolProduct.sqlite3')
cursor = conn.cursor()

# Write your join query
query = '''
SELECT *
FROM TBL_Product TP
JOIN TBL_Comment TC
ON TP.Product_Id = TC.Product_Id
'''

# Execute the query
cursor.execute(query)

# Fetch all rows from the executed query
rows = cursor.fetchall()

# Get the column names from the cursor
column_names = [description[0] for description in cursor.description]

# Open a CSV file for writing
with open('joined_table.csv', 'w', newline='') as csvfile:
    csvwriter = csv.writer(csvfile)
    
    # Write the column names to the CSV file
    csvwriter.writerow(column_names)
    
    # Write the rows to the CSV file
    csvwriter.writerows(rows)

# Close the database connection
conn.close()
