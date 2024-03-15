import argparse
import os

from db_utils import export_table_to_csv, create_db_connection

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='Create schema2.',
        usage='download_sakila_schema2.py [<args>] [-h | --help]'
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

    # --------------------- Schema 2 -------------------
    query_order = " order by rand() "

    # Table Join of address, city and country
    query_select = "a.address_id, a.address, a.address2, a.district, a.postal_code, a.phone, c.city_id, c.city, co.country_id, co.country"
    query_from = "address a, city c, country co"
    query_where = "a.city_id = c.city_id AND c.country_id = co.country_id"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + " where " + query_where + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='address',
        exclude_attributes=exclude_attributes
    )

    # Table Join of payment and rental
    query_select = "p.payment_id, p.customer_id, p.staff_id as pstaff_id, p.amount, p.payment_date, r.rental_id, r.rental_date, r.inventory_id, r.customer_id, r.return_date, r.staff_id as rstaff_id"
    query_from = "payment p, rental r"
    query_where = "p.rental_id = r.rental_id"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + " where " + query_where + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='payment',
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

    # Remaining Table language
    query_select = "language_id, name"
    query_from = "language"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='language',
        exclude_attributes=exclude_attributes
    )

    # Remaining Table film
    query_select = "film_id, title, description, release_year, language_id, original_language_id, rental_duration, rental_rate, length, replacement_cost, rating"
    query_from = "film"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='film',
        exclude_attributes=exclude_attributes
    )

    # Remaining Table film_category
    query_select = "film_id, category_id"
    query_from = "film_category"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='film_category',
        exclude_attributes=exclude_attributes
    )

    # Remaining Table category
    query_select = "category_id, name"
    query_from = "category"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='category',
        exclude_attributes=exclude_attributes
    )

    # Remaining Table staff
    query_select = "staff_id, first_name, last_name, address_id, email, store_id, active, username, password"
    query_from = "staff"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='staff',
        exclude_attributes=exclude_attributes
    )

    # Remaining Table customer
    query_select = "customer_id, store_id, first_name, last_name, email, address_id, active, create_date"
    query_from = "customer"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='customer',
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
