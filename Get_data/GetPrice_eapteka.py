import time
import psycopg2
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.support.wait import WebDriverWait

# Настройка опций Chrome для Selenium WebDriver
chrome_options = webdriver.ChromeOptions()
chrome_options.add_argument('--user-data-dir=C:\\Users\\Kilami\\AppData\\Local\\Google\\Chrome\\User Data\\Profile 2')
chrome_options.add_argument("--disable-geolocation")
chrome_options.add_argument("--disable-extensions")

# Чтение ссылок и информации о городах из файла
with open('links_cities_eapteka.txt', 'r', encoding='utf-8') as file:
    strs = file.readlines()

# Установка соединения с базой данных PostgreSQL
conn = psycopg2.connect(
    dbname="",
    user="",
    password="",
    host="",
    port=""
)


def fetch_data_for_substance(active_substance):
    # Цикл по каждой строке из файла для извлечения информации о городе, регионе и URL
    for line in strs:
        cur = conn.cursor()
        url = line.split(" - ")[0].strip()
        line = line.replace(url, "").strip()
        line = line.replace("-", "", 1).strip()
        city = line.split(", ")[0].strip()
        region = line.split(", ")[1].strip()
        url = url + "search/?q=" + active_substance

        try:
            # Инициализация WebDriver
            driver = webdriver.Chrome(options=chrome_options)
            driver.get(url)
            wait = WebDriverWait(driver, 10)

            # Парсинг кода страницы с помощью BeautifulSoup
            html_code = driver.page_source
            soup = BeautifulSoup(html_code, 'html.parser')


            links = []
            check_links = soup.find('div', class_='listing cc-group search-context')
            if check_links is not None:
                links = soup.find('div', class_='listing cc-group search-context').find_all('section',
                                                                                            class_='listing-card js-neon-item')
            second_links = soup.find('div', class_='listing cc-group').find_all('section',
                                                                                class_='listing-card js-neon-item')
            links.extend(second_links)


            for link in links:
                description = link.find('h5', class_='listing-card__title').find('a').text.strip()
                href = link.find('a', class_='listing-card__title-link')['href']
                upper_price_element = link.find('div', class_='listing-card__price-wrapper').find('span', class_='listing-card__price-old')[
                    'data-old-price']
                manufacturer = link.find('span', class_='listing-card__manufacturer').find_next_sibling(
                    'a').text.strip()

                # Определение цены на основе доступных элементов
                if upper_price_element != '':
                    price = link.find('div', class_='listing-card__price-wrapper').find('span',
                                                                                        class_='listing-card__price-old')[
                        'data-old-price']
                else:
                    price = link.find('span', class_='listing-card__price-new')['data-price']

                price = price.replace(" ", "")

                # Вставка данных в таблицу базы данных
                sql = "INSERT INTO cities_price_eapteka (city, region, price, description, active_substance, manufacturer, link) VALUES (%s, %s, %s, %s, %s, %s, %s)"
                cur.execute(sql, (city, region, price, description, active_substance, manufacturer, href))
                conn.commit()

            time.sleep(10)  # Пауза в 10 секунд перед следующей итерацией
            driver.quit()

        except Exception as e:
            print(f"Ошибка при запросе: {e}")

# Вызов функции с заданным активным веществом
fetch_data_for_substance("Умифеновир")
