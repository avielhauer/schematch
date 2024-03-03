import argparse
import os

from db_utils import export_table_to_csv, create_db_connection

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='Create schema1.',
        usage='download_sakila_schema1.py [<args>] [-h | --help]'
    )
    parser.add_argument('--host', type=str, default="relational.fit.cvut.cz")
    parser.add_argument("--database", type=str, required=True)
    parser.add_argument("--user", type=str, default="guest")
    parser.add_argument("--password", type=str, default='relational')
    parser.add_argument("--exclude_attributes", type=str, nargs='+')
    parser.add_argument("--save_path", type=str, required=True)
    args = parser.parse_args()

    exclude_attributes = args.exclude_attributes
    save_path = os.path.join(args.save_path, 'tables')

    db_connection = create_db_connection(host=args.host, database=args.database, user=args.user, password=args.password)

    # --------------------- Schema 1 -------------------
    query_order = " order by rand() "

    # Table Join of customer and address
    query_select = "c.customer_id, c.store_id, c.first_name, c.last_name, c.email, c.active, c.create_date, a.address_id, a.address, a.address2, a.district, a.city_id, a.postal_code, a.phone"
    query_from = "customer c, address a"
    query_where = "c.address_id=a.address_id"
    query_limit = "300"
    show_db_query = "select " + query_select + " from " + query_from + " where " + query_where + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='customer',
        exclude_attributes=exclude_attributes
    )

    # Table Join of city and country
    query_select = "c.city_id, c.city, co.country_id, co.country"
    query_from = "city c, country co"
    query_where = "c.country_id=co.country_id"
    query_limit = "300"
    show_db_query = "select " + query_select + " from " + query_from + " where " + query_where + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='city',
        exclude_attributes=exclude_attributes
    )

    # Table Join of film and language (ohne special features da set-based)
    query_select = (
        "f.film_id, f.title, f.description, f.release_year, l.name as language, f.original_language_id, f.rental_duration, f.rental_rate, f.length, f.replacement_cost, f.rating")
    query_from = "film f, language l"
    query_where = "f.language_id=l.language_id"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + " where " + query_where + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='film',
        exclude_attributes=exclude_attributes
    )

    # Table Join of film_category and category
    query_select = "f.film_id, c.category_id, c.name as category_name"
    query_from = "film_category f, category c"
    query_where = "f.category_id=c.category_id"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + " where " + query_where + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='film_category',
        exclude_attributes=exclude_attributes
    )

    # Table Join of staff, address, city and country
    query_select = "s.staff_id, s.first_name, s.last_name, s.email, s.store_id, s.active, s.username, s.password, a.address_id, a.address, a.address2, a.district,  a.postal_code, a.phone, c.city_id, c.city, co.country_id, co.country"
    query_from = "staff s, address a, city c, country co"
    query_where = "s.address_id = a.address_id AND a.city_id = c.city_id AND c.country_id = co.country_id"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + " where " + query_where + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='staff',
        exclude_attributes=exclude_attributes
    )

    # tables = ['payment', 'rental', 'inventory', 'film_actor', 'actor', 'store', 'language']
    # query_limit = "50000"
    # for table in tables:
    #    show_db_query = "select * from " + table + " limit " + query_limit
    #    with db_connection.cursor() as cursor:
    #        cursor.execute(show_db_query)
    #        header = [row[0] for row in cursor.description]
    #        rows = cursor.fetchall()
    #        export(db_connection, 'scenario1', schema_name, table, header, rows)

    # Remaining Table payment
    query_select = "payment_id, customer_id, staff_id, rental_id, amount, payment_date"
    query_from = "payment"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='payment',
        exclude_attributes=exclude_attributes
    )

    # Remaining Table rental
    query_select = "rental_id,  rental_date, inventory_id, customer_id, return_date, staff_id"
    query_from = "rental"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='rental',
        exclude_attributes=exclude_attributes
    )

    # Remaining Table inventory
    query_select = "inventory_id, film_id, store_id"
    query_from = "inventory"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='inventory',
        exclude_attributes=exclude_attributes
    )

    # Remaining Table film_actor
    query_select = "actor_id, film_id"
    query_from = "film_actor"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='film_actor',
        exclude_attributes=exclude_attributes
    )

    # Remaining Table actor
    query_select = "actor_id, first_name, last_name"
    query_from = "actor"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='actor',
        exclude_attributes=exclude_attributes
    )

    # Remaining Table store
    query_select = "store_id, manager_staff_id, address_id"
    query_from = "store"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='store',
        exclude_attributes=exclude_attributes
    )

    # Remaining Table language
    query_select = "language_id, name"
    query_from = "language"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='language',
        exclude_attributes=exclude_attributes
    )

    # Remaining Table address (only addresses of stores)
    query_select = "a.address_id, a.address, a.address2, a.district, a.city_id, a.postal_code, a.phone"
    query_from = "address a"
    query_where = "address_id IN (select address_id from store)"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + " where " + query_where + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='address',
        exclude_attributes=exclude_attributes
    )
