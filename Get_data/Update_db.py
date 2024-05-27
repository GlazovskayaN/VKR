import pandas as pd
import psycopg2
import re


# Функция для обновления информации о лекарствах из Excel файла в базе данных
def update_medicines_from_excel(file_path, apteka_name, active_sub):
        # Чтение данных из Excel файла
        excel_data = pd.read_excel(file_path)
        apteka = apteka_name
        # Создание курсора для выполнения SQL-запросов
        cursor = conn.cursor()

        # Итерация по строкам в DataFrame
        for index, row in excel_data.iterrows():
            description = row['description']
            manufacturer = row['manufacturer']
            quantity = row['quantity(mg)']
            other_substances = row['other_substances']
            active_substance =active_sub

            # Проверка наличия записи в базе данных с таким же названием лекарства и производителем
            if pd.notna(manufacturer):
                cursor.execute(
                    "SELECT * FROM cities_price_" + apteka + " WHERE description=%s AND manufacturer=%s AND active_substance=%s",
                    (description, manufacturer, active_substance))
            else:
                cursor.execute("SELECT * FROM cities_price_" + apteka + " WHERE description=%s AND active_substance=%s",
                               (description, active_substance))
            existing_record = cursor.fetchone()

            # Если количество равно 0, удалить запись из базы данных
            if pd.isna(quantity):
                if pd.notna(manufacturer):
                    cursor.execute(
                        "DELETE FROM cities_price_" + apteka + " WHERE description=%s AND manufacturer=%s AND active_substance=%s",
                        (description, manufacturer, active_substance))
                else:
                    cursor.execute(
                        "DELETE FROM cities_price_" + apteka + " WHERE description=%s AND manufacturer = '' AND active_substance=%s",
                        (description, active_substance))
            else:
                # Если количество не равно 0, обновить информацию о лекарстве
                if pd.notna(manufacturer):
                    cursor.execute(
                        "UPDATE cities_price_" + apteka + " SET \"quantity(mg)\"=%s, other_substances=%s WHERE description=%s AND manufacturer=%s AND active_substance=%s",
                        (quantity, other_substances, description, manufacturer, active_substance))
                else:
                    cursor.execute(
                        "UPDATE cities_price_" + apteka + " SET \"quantity(mg)\"=%s, other_substances=%s WHERE description=%s AND manufacturer = '' AND active_substance=%s",
                        (quantity, other_substances, description, active_substance))

        # Закрыть курсор и соединение с базой данных
        cursor.close()
        conn.commit()
        print("Обновление информации о лекарствах из Excel файла выполнено успешно.")



# Функция для обновления информации о количестве едениц лекарства в базе данных
def update_pieces_in_database(name_table):

        # Установление соединения с базой данных PostgreSQL
        cursor = conn.cursor()

        # Получение уникальных описаний лекарств из базы данных
        cursor.execute("SELECT DISTINCT description FROM " + name_table)
        rows = cursor.fetchall()

        # Регулярное выражение для поиска количества едениц лекарства в описании
        pattern = re.compile(r'(\d+)\s*шт\.?')

        # Обновление информации о количестве едениц лекарства в базе данных
        for row in rows:
            description = row[0]
            match = re.search(pattern, description)
            if match:
                pieces = int(match.group(1))
            else:
                pieces = 1

            cursor.execute("UPDATE " + name_table + " SET pieces=%s WHERE description=%s", (pieces, description))

        # Подтверждение транзакции и закрытие соединения
        conn.commit()
        print("Обновление информации о количестве таблеток в базе данных выполнено успешно.")


# Функция для вычисления цены за грамм и обновления этой информации в базе данных
def update_price_per_gram(name_table):

        # Установление соединения с базой данных PostgreSQL

        cursor = conn.cursor()

        # Получение всех записей из базы данных
        cursor.execute("SELECT id, price, pieces, \"quantity(mg)\" FROM " + name_table)
        rows = cursor.fetchall()

        # Обновление цены за грамм в базе данных
        for row in rows:
            id, price, pieces, quantity = row
            # Расчет цены за грамм
            if pieces != 0 and quantity != 0:
                price_per_gram = price / (pieces * quantity)
            else:
                price_per_gram = None

            # Обновление соответствующей строки в столбце с ценой за грамм
            cursor.execute(
                "UPDATE " + name_table + " SET price_per_mgram=%s WHERE id=%s",
                (price_per_gram, id))

        # Подтверждение транзакции и закрытие соединения
        conn.commit()
        print("Вычисление цены за грамм и обновление информации выполнено успешно.")

conn = psycopg2.connect(
    dbname="",
    user="",
    password="",
    host="",
    port=""
)

update_medicines_from_excel("File_path", "apteka", "Парацетамол")
update_pieces_in_database("table_name")
update_price_per_gram("table_name")
conn.close()