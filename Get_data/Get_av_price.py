import os
import re
import psycopg2
import openpyxl
import pandas as pd

# Функция для обработки файлов Excel и вычисления средних цен для каждого месяца
def process_excel_files(folder_path):
    # Создаем пустые словари для хранения данных
    result_dict = {}
    yearly_prices = {}

    # Проходим по всем файлам в папке
    for file_name in os.listdir(folder_path):
        if file_name.endswith('.xlsx'):
            file_path = os.path.join(folder_path, file_name)

            # Извлекаем название региона из имени файла
            region = re.split(r'\d{2}\.\d{2}\.\d{4}', file_name)[0].rstrip()
            region = region.replace("_", "")

            # Читаем файл Excel в DataFrame
            df = pd.read_excel(file_path, skiprows=1, decimal=',')

            # Приводим даты к формату datetime
            df['Дата'] = pd.to_datetime(df['Дата'], format='%d.%m.%Y')

            # Группируем данные по месяцам и вычисляем среднюю цену для каждого месяца
            df['Месяц'] = df['Дата'].dt.to_period('M')
            monthly_prices = df.groupby('Месяц')
            monthly_av_prices = {}
            for month, group_data in monthly_prices:
                prices = group_data.iloc[0, 1:-1]
                non_zero_prices = [price for price in prices if price is not None and price != 0]
                # Вычисляем среднее значение для текущего месяца
                average_price = sum(non_zero_prices) / len(non_zero_prices)
                # Сохраняем среднее значение для текущего месяца в словаре
                monthly_av_prices[month] = average_price

            result_dict[region] = monthly_av_prices
            yearly_price = sum(monthly_av_prices.values()) / len(monthly_av_prices)
            yearly_prices[region] = yearly_price

    # Создаем DataFrame из словаря
    result_df = pd.DataFrame.from_dict(result_dict, orient='index')
    result_df['Средняя цена за год'] = result_df.iloc[:, :-1].sum(axis=1) / 12
    # Выводим результат
    print(result_df)
    output_file = os.path.join(folder_path, "result.xlsx")
    result_df.to_excel(output_file)

    return result_dict, yearly_prices

# Функция для вычисления средних цен из базы данных PostgreSQL и сохранения результатов
def calculate_and_save_average_prices(name_table, av_name_table, active_substance):
    try:
        conn = psycopg2.connect(
            dbname="",
            user="",
            password="",
            host="",
            port=""
        )

        cursor = conn.cursor()

        # Получение уникальных городов
        cursor.execute("SELECT DISTINCT city, region FROM " + name_table)
        cities_regions = cursor.fetchall()

        for city, region in cities_regions:
            cursor.execute(
                "SELECT price FROM " + name_table + " WHERE city=%s AND region=%s AND active_substance=%s",
                (city, region, active_substance))
            prices = cursor.fetchall()
            cursor.execute(
                "SELECT price_per_mgram FROM " + name_table + " WHERE city=%s AND region=%s AND active_substance=%s",
                (city, region, active_substance))
            prices_per_mgram = cursor.fetchall()
            if prices:
                prices = [price[0] for price in prices]  # извлечение значений из кортежей
                average_price = sum(prices) / len(prices)
            else:
                average_price = None
            if prices_per_mgram:
                prices_per_mgram = [price_per_mgram[0] for price_per_mgram in prices_per_mgram]  # извлечение значений из кортежей
                average_price_per_mgram = sum(prices_per_mgram) / len(prices_per_mgram)
            else:
                average_price_per_mgram = None

            cursor.execute(
                "INSERT INTO " + av_name_table + " (city, region, active_substance, av_price, av_price_mg) VALUES (%s, %s, %s, %s,%s)",
                (city, region, active_substance, average_price, average_price_per_mgram))

        # Подтверждение транзакции и закрытие соединения
        conn.commit()
        conn.close()

        print("Средние цены успешно вычислены и сохранены в базе данных.")
    except Exception as e:
        print(f"Произошла ошибка: {e}")

# Путь к папке с файлами Excel
folder_path = "Path"

# Обработка файлов Excel и сохранение результатов
result_dict, yearly_prices = process_excel_files(folder_path)

# Вычисление средних цен из базы данных PostgreSQL и сохранение результатов
calculate_and_save_average_prices("tabel_name", "av_table_name", "Парацетамол")