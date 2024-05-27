import shutil
import requests
import os
import uuid
import time

# Функция для загрузки файла по URL
def download_file(url, filename):
    # Устанавливаем заголовок для запроса
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, как Gecko) Chrome/58.0.3029.110 Safari/537.36'
    }
    # Запрашиваем файл по URL
    response = requests.get(url, headers=headers, stream=True)
    if response.status_code == 200:
        # Сохраняем файл
        with open(filename, 'wb') as out_file:
            response.raw.decode_content = True
            shutil.copyfileobj(response.raw, out_file)
    time.sleep(5)  # Ожидаем 5 секунд

def download_and_move_file(url, destination_folder, i):
    try:
        # Формируем имя файла
        filename = "Protocol" + str(i) + ".xlsx"
        # Запрашиваем файл по URL
        response = requests.get(url, stream=True)
        if response.status_code == 200:
            # Создаем путь к месту назначения
            destination_path = os.path.join(destination_folder, filename)
            # Сохраняем файл в папку
            with open(destination_path, 'wb') as f:
                response.raw.decode_content = True
                shutil.copyfileobj(response.raw, f)
            print(f"Файл успешно загружен и помещен в папку: {filename}")
            return True
        else:
            print(f"Ошибка при загрузке файла: {response.status_code}")
            return False
    except Exception as e:
        print(f"Произошла ошибка: {e}")
        return False

# Функция для создания задачи конвертации
def create_conversion_job():
    # URL для создания задачи конвертации
    url = "https://api.api2convert.com/v2/jobs"
    # Заголовки для запроса
    headers = {
        'x-oc-api-key': 'api',
        'Content-Type': 'application/json',
        'Cache-Control': 'no-cache'
    }
    # Данные для запроса
    payload = {
        "conversion": [{
            "category": "document",
            "target": "xlsx"
        }]
    }
    # Отправляем запрос на создание задачи
    response = requests.post(url, headers=headers, json=payload)
    data = response.json()
    return data.get('id'), data.get('server')

# Функция для загрузки файла на сервер конвертации
def upload_file(file_path, server, job_id):
    if server and job_id:
        # Формируем URL для загрузки файла
        upload_url = f"{server}/upload-file/{job_id}"
        upload_uuid = str(uuid.uuid4())[:8]
        # Заголовки для запроса
        headers = {
            'x-oc-api-key': 'api',
            'x-oc-upload-uuid': upload_uuid,
            'Cache-Control': 'no-cache'
        }
        # Файлы для загрузки
        files = {
            'file': (os.path.basename(file_path), open(file_path, 'rb')),
            'decrypt_password': (None, '')  # Опциональное поле для пароля, если файл зашифрован
        }
        # Отправляем файл на сервер
        response = requests.post(upload_url, headers=headers, files=files)
        if response.status_code == 200:
            return True
    return False

# Функция для получения ссылки на конвертированный файл
def get_converted_file_url(api_key, job_id):
    # Формируем URL для получения информации о задаче
    url = f"https://api.api2convert.com/v2/jobs/{job_id}"
    headers = {'x-oc-api-key': api_key}
    while True:
        # Запрашиваем информацию о задаче
        response = requests.get(url, headers=headers)
        data = response.json()
        # Проверяем, готов ли конвертированный файл
        if 'output' in data and data['output']:
            output_info = data['output'][0]
            return output_info['uri']
        if not data['process']:
            return None
        # Проверяем наличие ошибок
        if 'errors' in data and data['errors']:
            print("Файл не был конвертирован из-за ошибок:", data['errors'])
            return 1
        time.sleep(5)  # Ожидаем 5 секунд перед следующим запросом

if __name__ == "__main__":
    file_path = 'protocols_links.txt'
    i = 1
    with open(file_path, 'r') as file:
        for line in file:
            url = "https:" + line.strip()
            pdf_file_path = "downloadedFile.pdf"
            download_file(url, pdf_file_path)
            print(url)

            # Создаем задачу конвертации
            job_id, server = create_conversion_job()
            if job_id and server:
                # Загружаем PDF файл для конвертации
                if upload_file(pdf_file_path, server, job_id):
                    print("PDF файл успешно загружен для конвертации.")
                    print("Дождитесь завершения конвертации.")

                    api_key = 'api'
                    # Получаем ссылку на конвертированный файл
                    converted_file_url = get_converted_file_url(api_key, job_id)
                    while not converted_file_url:
                        print("Конвертация еще не завершена. Подождите...")
                        time.sleep(10)
                        converted_file_url = get_converted_file_url(api_key, job_id)
                        if converted_file_url == 1:
                            break
                    # Загружаем и перемещаем конвертированный файл
                    download_and_move_file(converted_file_url, "Protocols", i)
                    print("Ссылка на конвертированный файл:", converted_file_url)
                else:
                    print("Не удалось загрузить PDF файл.")
            else:
                print("Не удалось создать задачу конвертации.")
            i += 1