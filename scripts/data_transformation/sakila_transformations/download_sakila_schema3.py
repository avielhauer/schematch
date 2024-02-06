import argparse
import os

from db_utils import export_table_to_csv, create_db_connection

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='Create schema3.',
        usage='download_sakila_schema3.py [<args>] [-h | --help]'
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

    # --------------------- Schema 3 -------------------
    query_order = " order by rand() "

    # Table Join of rental and inventory
    query_select = "r.rental_id, r.rental_date, r.customer_id, r.return_date, r.staff_id, i.inventory_id, i.film_id, i.store_id"
    query_from = "rental r, inventory i"
    query_where = "r.inventory_id = i.inventory_id"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + " where " + query_where + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='rental',
        exclude_attributes=exclude_attributes
    )

    # Table Join of actor and film_actor
    query_select = "f.film_id, a.actor_id, a.first_name, a.last_name "
    query_from = "film_actor f, actor a"
    query_where = "f.actor_id = a.actor_id"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + " where " + query_where + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='film_actor',
        exclude_attributes=exclude_attributes
    )

    # Table Join of store and address
    query_select = "s.store_id, s.manager_staff_id, a.address_id, a.address, a.address2, a.district, a.city_id, a.postal_code, a.phone"
    query_from = "store s, address a"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='store',
        exclude_attributes=exclude_attributes
    )

    # Table Join of Film and 2 times language
    query_select = (
        "f.film_id, f.title, f.description, f.release_year, l1.name as language, l2.name as original_language, f.rental_duration, f.rental_rate, f.length, f.replacement_cost, f.rating")
    query_from = "film f, language l1, language l2"
    query_where = "f.language_id = l1.language_id AND f.original_language_id = l2.language_id"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + " where " + query_where + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='film',
        exclude_attributes=exclude_attributes
    )

    # Remaining Table payment
    query_select = "payment_id, customer_id, staff_id, rental_id, amount, payment_date"
    query_from = "payment"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='payment',
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

    # Remaining Table Address (in staff and customer)
    query_select = "a.address_id, a.address, a.address2, a.district, a.city_id, a.postal_code, a.phone"
    query_from = "address a"
    query_where = "address_id IN (select address_id from staff) OR address_id IN (select address_id from customer)"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + " where " + query_where + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='address',
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

    # Remaining Table city
    query_select = "city_id, city, country_id"
    query_from = "city"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='city',
        exclude_attributes=exclude_attributes
    )

    # Remaining Table country
    query_select = "country_id, country"
    query_from = "country"
    query_limit = "50000"
    show_db_query = "select " + query_select + " from " + query_from + query_order + " limit " + query_limit

    export_table_to_csv(
        db_connection, query=show_db_query, save_path=save_path, table_name='country',
        exclude_attributes=exclude_attributes
    )
