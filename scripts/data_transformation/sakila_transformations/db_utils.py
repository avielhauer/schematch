import os
import mysql.connector as mysql
import pandas as pd
from pathlib import Path
from typing import List


def run_query(db_connection, query: str):
    with db_connection.cursor() as cursor:
        cursor.execute(query)
        for db in cursor:
            print(db)


def fetch_table_data(db_connection, query: str):
    cursor = db_connection.cursor()
    cursor.execute(query)

    header = [row[0] for row in cursor.description]

    rows = cursor.fetchall()

    return header, rows


def export_table_to_csv(db_connection, save_path: str, exclude_attributes: List[str] = None, query: str = None,
                        table_name: str = None):
    if table_name is None and query is None:
        raise ValueError("At least one between table_name and query has to be specified!")
    if table_name is None:
        table_name = query
    if query is None:
        query = f'select * from {table_name}'

    header, rows = fetch_table_data(db_connection, query)

    if not os.path.exists(save_path):
        Path(save_path).mkdir(parents=True, exist_ok=True)

    # Create csv file
    out_path = os.path.join(save_path, f'{table_name}.csv')
    df = pd.DataFrame(data=rows, columns=header)
    # Remove duplicate columns
    df = df.loc[:, ~df.columns.duplicated()].copy()
    if exclude_attributes is not None:
        for drop_col in exclude_attributes:
            if drop_col in df.columns:
                df = df.drop([drop_col], axis=1)
    df.to_csv(out_path, index=False)

    print(f"Exported table {table_name}.")


def create_db_connection(host: str, database: str, user: str, password: str):
    return mysql.connect(host=host, database=database, user=user, password=password)


def get_tables(db_connection, exclude_tables: List[str] = None):
    show_db_query = "show tables"
    tables = []
    with db_connection.cursor() as cursor:
        cursor.execute(show_db_query)
        for db in cursor:
            tables.append(db[0])

    if exclude_tables is not None:
        tables = [x for x in tables if x not in exclude_tables]
    return tables


def export_db(db_connection, save_path: str, exclude_tables: List[str] = None, exclude_attributes: List[str] = None):
    tables = get_tables(db_connection, exclude_tables)

    save_path = os.path.join(save_path, 'tables')
    if not os.path.exists(save_path):
        Path(save_path).mkdir(parents=True, exist_ok=True)

    for table in tables:
        export_table_to_csv(
            db_connection=db_connection, table_name=table, save_path=save_path, exclude_attributes=exclude_attributes
        )
