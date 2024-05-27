import re
import time
import psycopg2
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.chrome.options import Options

# Чтение ссылок и информации о городах из файла
with open('links_cities_uteka.txt', 'r', encoding='utf-8') as file:
    strs = file.readlines()

# Установка соединения с базой данных PostgreSQL
conn = psycopg2.connect(
    dbname="",
    user="",
    password="",
    host="",
    port=""
)

# Настройка опций Chrome для Selenium WebDriver
options = Options()
options.headless = True

def fetch_data_for_substance(active_substance):
    # Цикл по каждой строке из файла для извлечения информации о городе, регионе и URL
    for line in strs:
        cur = conn.cursor()
        url = line.split(" - ")[0].strip()
        line = line.replace(url, "").strip()
        line = line.replace("-", "", 1).strip()
        city = line.split(", ")[0].strip()
        region = line.split(", ")[1].strip()
        url = url + "search/?query=" + active_substance + "&aq=" + active_substance + "&ac=text"

        try:
            # Инициализация WebDriver
            driver = webdriver.Chrome(options=options)
            driver.get(url)
            wait = WebDriverWait(driver, 10)  # Ожидание до 10 секунд

            # Парсинг кода страницы с помощью BeautifulSoup
            html_code = driver.page_source
            soup = BeautifulSoup(html_code, 'html.parser')

            # Поиск всех частей html када, содержащих необходимую информацию
            links = soup.find('div', class_='product-list-grid').find_all(
                'div', class_=['product-preview ui-panel ui-panel_size_s ui-panel_clickable _advertising',
                               'product-preview ui-panel ui-panel_size_s ui-panel_clickable'])

            # Извлечение и сохранение информации из каждой части
            for link in links:
                description = link.find('div', class_='product-preview__inner').find('span', itemprop='name').text.strip()
                href = link.find('div', class_='product-preview__inner').find('a')['href']
                check_manufacturer = link.find('div', class_='product-preview__sub-title')
                manufacturer = check_manufacturer.text.strip() if check_manufacturer else ''

                pr_price = link.find('div', class_='product-price product-preview-offer__product-price _block')
                if pr_price is not None:
                    price = pr_price.find('div', class_='ui-price__content').text.strip()
                    price = re.sub(r'\s+', '', price.replace("от", "").replace("₽", "").strip())

                    # Вставка данных в таблицу базы данных
                    sql = "INSERT INTO cities_price_uteka (city, region, price, description, active_substance, manufacturer, link) VALUES (%s, %s, %s, %s, %s, %s, %s)"
                    cur.execute(sql, (city, region, price, description, active_substance, manufacturer, href))
                    conn.commit()

            time.sleep(10)  # Пауза в 10 секунд перед следующей итерацией
            driver.quit()

        except Exception as e:
            print(f"Ошибка при запросе: {e}")

# Вызов функции с заданным активным веществом
fetch_data_for_substance("Умифеновир")

