import time
import psycopg2
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

# Чтение ссылок и информации о городах из файла
with open('links_cities_zdravcity.txt', 'r', encoding='utf-8') as file:
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
        url = url + "?what=" + active_substance

        try:
            # Инициализация WebDriver
            driver = webdriver.Chrome(options=options)
            driver.get(url)

            wait = WebDriverWait(driver, 10)  # Ожидание до 10 секунд

            # Ожидание загрузки элемента с нужным классом
            wait.until(EC.presence_of_element_located((By.CLASS_NAME, "ProductsList_list-grid__76wlE")))

            html_code = driver.page_source  # Получаем HTML-код страницы после загрузки всех элементов
            soup = BeautifulSoup(html_code, 'html.parser')
            links = soup.find('div', class_='ProductsList_list-grid__76wlE').find_all(
                'div', class_='ProductsList_list-preview__OIemJ')

            # Извлечение и сохранение информации из каждой найденной части html кода
            for link in links:
                description = link.find('a', class_='lazy-image LazyImage_lazy-image-container__eQ_7n')['aria-label']
                href = link.find('a', class_='lazy-image LazyImage_lazy-image-container__eQ_7n')['href']
                info = link.find('div', class_='HorizontalInfoList_list__IOlDa')
                manufacturer = info.find('span', class_='HorizontalInfoList_list-item-label__aV5qZ',
                                         text='Производитель').find_next_sibling('span').text
                check_country = info.find('span', class_='HorizontalInfoList_list-item-label__aV5qZ',
                                          text='Страна производства')
                country = check_country.find_next_sibling('span').text if check_country is not None else ''

                price = ""
                upper_price_element = link.find('div',
                                                class_='Price_price__Y1FnU Price_common-price-currency-upper__OP_Fh')
                if upper_price_element is not None:
                    price = upper_price_element.text.strip()
                else:
                    last_price = link.find('div',
                                           class_='Price_common-price__v9UOU Horizontal_horizontal-last-price__3S9E3')
                    pr_price = link.find('div', class_='Price_common-price__v9UOU')
                    if last_price is None and pr_price is not None:
                        price = link.find('div',
                                          class_='Price_price__Y1FnU Price_common-price-currency__2qnhU Horizontal_horizontal-price-container-price-currency__RdGtm').text.strip()

                if price != "":
                    price = price.replace("Цена", "").replace("₽", "").replace("от", "").replace(" ", "").strip()
                    sql = "INSERT INTO cities_price_zdravcity (city, region, price, description, link, active_substance, manufacturer, country) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"
                    cur.execute(sql, (city, region, price, description, href, active_substance, manufacturer, country))
                    conn.commit()

            driver.quit()
            time.sleep(10)  # Пауза в 10 секунд перед следующей итерацией
        except Exception as e:
            print(f"Ошибка при запросе: {e}")


# Вызов функции с заданным активным веществом
fetch_data_for_substance("Умифеновир")