import psycopg2

def connect_to_db():
    db_settings = {
        "dbname": "teaching2025",
        "user": "postgres",
        "password": "123",
        "host": "localhost",
        "port": "5432"
    }

    try:
        connection = psycopg2.connect(**db_settings)
        print("The connection to the database has been established successfully.")
        return connection
    except psycopg2.Error as e:
        print(f"Error connecting to database: {e}")
        return None


def fetch_course_instances(connection):
    try:
        cursor = connection.cursor()

        query = """
            SELECT ci.instance_id,
                   cl.course_name,
                   ci.period,
                   ci.year,
                   ci.num_students
            FROM courseinstance ci
            JOIN courselayout cl
              ON ci.course_code = cl.course_code
             AND ci.version_no = cl.version_no
            ORDER BY ci.year DESC, ci.period, ci.instance_id;
        """

        cursor.execute(query)
        rows = cursor.fetchall()

        print("Course Instances:")
        for r in rows:
            print(f"Instance ID: {r[0]}, Course: {r[1]}, Period: {r[2]}, Year: {r[3]}, Students: {r[4]}")

    except psycopg2.Error as e:
        print(f"Error executing request: {e}")
    finally:
        cursor.close()


def main():
    connection = connect_to_db()
    if connection:
        fetch_course_instances(connection)
        connection.close()
        print("The database connection was closed.")


if __name__ == "__main__":
    main()

