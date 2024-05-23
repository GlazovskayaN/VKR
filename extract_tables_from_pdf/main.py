import pdfplumber
import pandas as pd
import camelot
import fitz
import tabula
from tabulate import tabulate
import PyPDF2

# Функция для извлечения таблицы с помощью pdfplumber
def extract_table_with_pdfplumber(pdf_path, page_number, table_settings):
    with pdfplumber.open(pdf_path) as pdf:
        page = pdf.pages[page_number]
        table = page.extract_table(table_settings)
    df = pd.DataFrame(table[1:], columns=table[0])
    return df

# Класс для конвертации PDF в изображения с помощью fitz (PyMuPDF)
class ConversionBackend:
    def convert(self, pdf_path, png_path):
        doc = fitz.open(pdf_path)
        for page in doc.pages():
            pix = page.get_pixmap()
            pix.save(png_path)

# Функция для извлечения таблиц с помощью Camelot
def extract_table_with_camelot(pdf_path):
    tables = camelot.read_pdf(pdf_path,
                              backend=ConversionBackend(),
                              strip_text='\n',
                              pages='all',
                              copy_text=['h'])
    # Предполагается, что нужна 16-я таблица (индекс 15)
    if len(tables) > 15:
        df = tables[15].df
    else:
        df = pd.DataFrame()
    return df

# Функция для извлечения таблиц с помощью Tabula
def extract_table_with_tabula(pdf_path, page_number):
    tables = tabula.read_pdf(pdf_path, pages=page_number)
    if tables:
        table = tabulate(tables[0])
    else:
        table = ""
    return table

# Функция для извлечения текста из PDF и создания DataFrame с помощью PyPDF2
def extract_text_with_pypdf2(pdf_path):
    pdf_file = open(pdf_path, 'rb')
    pdf_reader = PyPDF2.PdfReader(pdf_file)
    table_data = []

    for page_num in range(len(pdf_reader.pages)):
        page = pdf_reader.pages[page_num]
        text = page.extract_text()
        lines = text.split('\n')

        for line in lines:
            row = line.split()
            table_data.append(row)

    df = pd.DataFrame(table_data)
    return df

# Основной код выполнения
if __name__ == "__main__":
    pdf_path = "Path_to_PDF.pdf"
    page_number = 4
    table_settings = {
        "vertical_strategy": "text",
        "horizontal_strategy": "text"
    }

    # Извлечение таблицы с помощью pdfplumber
    df_pdfplumber = extract_table_with_pdfplumber(pdf_path, page_number, table_settings)
    print("PDFPlumber DataFrame:")
    print(df_pdfplumber)

    # Извлечение таблицы с помощью Camelot
    df_camelot = extract_table_with_camelot(pdf_path)
    print("\nCamelot DataFrame:")
    print(df_camelot)

    # Извлечение таблицы с помощью Tabula
    tabula_table = extract_table_with_tabula(pdf_path, 6)
    print("\nTabula Table:")
    print(tabula_table)

    # Извлечение текста с помощью PyPDF2 и преобразование в DataFrame
    df_pypdf2 = extract_text_with_pypdf2(pdf_path)
    print("\nPyPDF2 DataFrame:")
    print(df_pypdf2)