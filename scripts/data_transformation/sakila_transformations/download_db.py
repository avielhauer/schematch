import argparse

from db_utils import export_db, create_db_connection


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='Download database.',
        usage='download_db.py [<args>] [-h | --help]'
    )
    parser.add_argument('--host', type=str, default="relational.fit.cvut.cz")
    parser.add_argument("--database", type=str, required=True)
    parser.add_argument("--user", type=str, default="guest")
    parser.add_argument("--password", type=str, default='relational')
    parser.add_argument("--exclude_tables", type=str, nargs='+')
    parser.add_argument("--exclude_attributes", type=str, nargs='+')
    parser.add_argument("--save_path", type=str, required=True)
    args = parser.parse_args()

    db_connection = create_db_connection(host=args.host, database=args.database, user=args.user, password=args.password)
    export_db(db_connection=db_connection, save_path=args.save_path, exclude_tables=args.exclude_tables,
              exclude_attributes=args.exclude_attributes)
