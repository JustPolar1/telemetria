"""Build the HU-01 technical report from preserved execution evidence."""
from pathlib import Path
from io import BytesIO
import json
from xml.etree import ElementTree
from PIL import Image as PILImage
from reportlab.pdfgen import canvas
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.styles import ParagraphStyle
from reportlab.platypus import Paragraph, Table, TableStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.utils import ImageReader

ROOT = Path(__file__).resolve().parents[2]
EVIDENCE = ROOT / 'incident-service' / 'evidence'
OUTPUT = ROOT / 'output' / 'pdf' / 'HU-01_Reporte_tecnico.pdf'
OUTPUT.parent.mkdir(parents=True, exist_ok=True)
SONAR = json.loads((EVIDENCE / 'sonar-results.json').read_text(encoding='utf-8-sig'))
MEASURES = {item['metric']: item['value'] for item in SONAR['measures']}
RESPONSES = json.loads((EVIDENCE / 'swagger-responses.json').read_text(encoding='utf-8'))
COUNTERS = {item.attrib['type']: item.attrib for item in ElementTree.parse(EVIDENCE / 'jacoco' / 'jacoco.xml').getroot().findall('counter')}
LINES = COUNTERS['LINE']
covered = int(LINES['covered'])
total = covered + int(LINES['missed'])
assert SONAR['qualityGate']['status'] == 'OK'
assert all(MEASURES[key] == '0' for key in ('bugs', 'vulnerabilities', 'code_smells', 'test_errors', 'test_failures'))
assert int(MEASURES['tests']) == 12
assert [item['status'] for item in RESPONSES] == [201, 200, 200, 200, 204, 404, 400]

fonts = Path('C:/Windows/Fonts')
if (fonts / 'segoeui.ttf').exists():
    pdfmetrics.registerFont(TTFont('Body', str(fonts / 'segoeui.ttf')))
    pdfmetrics.registerFont(TTFont('Bold', str(fonts / 'segoeuib.ttf')))
    pdfmetrics.registerFontFamily('Body', normal='Body', bold='Bold', italic='Body', boldItalic='Bold')
else:
    pdfmetrics.registerFontFamily('Body', normal='Helvetica', bold='Helvetica-Bold')
    pdfmetrics.registerFontFamily('Bold', normal='Helvetica-Bold')

INK = colors.HexColor('#172C45')
TEAL = colors.HexColor('#007E87')
MUTED = colors.HexColor('#536577')
LIGHT = colors.HexColor('#EDF5F6')
LINE = colors.HexColor('#D5E0E5')
BODY = ParagraphStyle('Body', fontName='Body', fontSize=10.5, leading=15, textColor=INK)
SMALL = ParagraphStyle('Small', parent=BODY, fontSize=8.5, leading=12)
CELL = ParagraphStyle('Cell', parent=BODY, fontSize=9.2, leading=12.5)
HEAD = ParagraphStyle('Head', parent=CELL, fontName='Bold', textColor=colors.white)

pdf = canvas.Canvas(str(OUTPUT), pagesize=A4)
pdf.setTitle('HU-01 - Microservicio CRUD con MongoDB y calidad de código')
pdf.setAuthor('ADAN GIOVANNI RAMIREZ ERIVES')
pdf.setSubject('Reporte técnico de laboratorio: Incident Service')
page_number = 0
width, height = A4
y = 0

def paragraph(text, style=BODY, x=48, w=None, gap=10):
    global y
    p = Paragraph(text, style)
    _, h = p.wrap(w or (width - 96), height)
    if y - h < 48:
        raise ValueError(f'Page {page_number} overflows: {text[:80]}')
    p.drawOn(pdf, x, y - h)
    y -= h + gap

def section(text):
    global y
    y -= 6
    pdf.setFont('Bold', 14)
    pdf.setFillColor(TEAL)
    pdf.drawString(48, y - 14, text)
    y -= 31

def table(headers, rows, widths):
    global y
    data = [[Paragraph(str(value), HEAD) for value in headers]]
    data.extend([[Paragraph(str(value), CELL) for value in row] for row in rows])
    obj = Table(data, colWidths=widths, hAlign='LEFT')
    obj.setStyle(TableStyle([
        ('BACKGROUND',(0,0),(-1,0),INK),
        ('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.white,LIGHT]),
        ('VALIGN',(0,0),(-1,-1),'TOP'),
        ('LEFTPADDING',(0,0),(-1,-1),9),('RIGHTPADDING',(0,0),(-1,-1),9),
        ('TOPPADDING',(0,0),(-1,-1),7),('BOTTOMPADDING',(0,0),(-1,-1),7),
        ('LINEBELOW',(0,-1),(-1,-1),0.5,LINE),
    ]))
    _, h = obj.wrap(width-96, height)
    if y-h<48: raise ValueError(f'Table overflows page {page_number}')
    obj.drawOn(pdf,48,y-h)
    y-=h+14

def page(title, wide=False):
    global page_number, width, height, y
    if page_number: pdf.showPage()
    page_number += 1
    width,height = landscape(A4) if wide else A4
    pdf.setPageSize((width,height))
    pdf.setFillColor(colors.white)
    pdf.rect(0,0,width,height,fill=1,stroke=0)
    pdf.setFillColor(TEAL)
    pdf.rect(0,height-8,width,8,fill=1,stroke=0)
    pdf.setFont('Body',8)
    pdf.setFillColor(MUTED)
    pdf.drawString(48,height-30,'HU-01  /  DESARROLLO WEB INTEGRAL')
    pdf.drawRightString(width-48,height-30,'INCIDENT SERVICE')
    pdf.setStrokeColor(LINE)
    pdf.line(48,39,width-48,39)
    pdf.drawString(48,25,'Reporte técnico de laboratorio  |  13 de noviembre de 2026')
    pdf.drawRightString(width-48,25,str(page_number))
    y=height-58
    if title:
        paragraph(title,ParagraphStyle('Title',parent=BODY,fontName='Bold',fontSize=23,leading=29),gap=18)
        pdf.bookmarkPage(f'page-{page_number}')
        pdf.addOutlineEntry(title,f'page-{page_number}',0,False)

def evidence_page(title, filename, caption):
    global y
    page(title,wide=True)
    paragraph(caption,SMALL,gap=13)
    image = PILImage.open(EVIDENCE / filename)
    available_h = y - 62
    scale = min((width-96)/image.width,available_h/image.height)
    iw,ih=image.width*scale,image.height*scale
    pdf.drawImage(ImageReader(image),48+(width-96-iw)/2,y-ih,width=iw,height=ih)

page('')
pdf.setFillColor(INK)
pdf.rect(0,height-330,width,330,fill=1,stroke=0)
pdf.setFillColor(colors.HexColor('#61D5D3'))
pdf.setFont('Bold',11)
pdf.drawString(48,height-70,'HU-01  /  PRÁCTICA DE LABORATORIO')
pdf.setFillColor(colors.white)
pdf.setFont('Bold',33)
pdf.drawString(48,height-133,'Microservicio CRUD')
pdf.drawString(48,height-178,'con MongoDB')
pdf.setFont('Body',16)
pdf.drawString(48,height-225,'Calidad de código y pruebas automatizadas')
pdf.setFont('Body',11)
pdf.drawString(48,height-269,'Implementación RESTful de incidencias de sensores')
y=height-371
paragraph('REPORTE TÉCNICO',ParagraphStyle('Label',parent=SMALL,textColor=TEAL,fontName='Bold'),gap=17)
paragraph('ADAN GIOVANNI RAMIREZ ERIVES',ParagraphStyle('Author',parent=BODY,fontName='Bold',fontSize=17,leading=24),gap=16)
paragraph('Asignatura: Desarrollo Web Integral<br/>Unidad II · Modalidad individual<br/>Fecha del reporte: 13 de noviembre de 2026',gap=25)
pdf.setFillColor(LIGHT)
pdf.roundRect(48,y-79,width-96,79,8,fill=1,stroke=0)
y-=14
paragraph('12 pruebas aprobadas · 94.87 % de líneas',ParagraphStyle('Status',parent=BODY,fontName='Bold',fontSize=13,leading=18),x=63,w=width-126,gap=5)
paragraph('Quality Gate de SonarQube: APROBADO',BODY,x=63,w=width-126,gap=33)
paragraph('Repositorio público: <link href="https://github.com/JustPolar1/telemetria" color="#007E87">github.com/JustPolar1/telemetria</link><br/>Rama de entrega: feature/incidents-mongodb-hu01',SMALL)

page('1. Microservicio y persistencia NoSQL')
paragraph('Se implementó <b>Incident Service</b>, un microservicio REST independiente para administrar incidencias de sensores. Utiliza Java 21, Spring Boot 4.1.1 y Spring Data MongoDB. Los servicios previos de parcelas y telemetría conservan su persistencia PostgreSQL; el cumplimiento de HU-01 corresponde al nuevo módulo.')
section('Arquitectura y responsabilidad')
paragraph('Swagger UI / cliente HTTP → IncidentController → IncidentService → IncidentRepository → MongoDB. El controlador valida entradas; el servicio aplica la lógica de negocio; el repositorio persiste documentos en la colección <b>incidents</b>. Las excepciones se traducen mediante IncidentExceptionHandler.')
table(['Campo','Regla o responsabilidad'],[
    ('id / folio','Identificador MongoDB y folio UUID generado, con índice único.'),
    ('sensorId','Obligatorio; hasta 100 caracteres alfanuméricos, guiones o guiones bajos.'),
    ('titulo / descripcion','Obligatorios; máximo 150 y 2000 caracteres. Se recortan los extremos.'),
    ('estado','ABIERTA, EN_REVISION o RESUELTA; obligatorio.'),
    ('fechaCreacion','Fecha UTC generada por el servicio; se conserva al actualizar.'),
],[113,width-209])
section('Contrato REST')
paragraph('Base: <b>/api/v1/incidents</b>. El listado acepta page ≥ 0 y size entre 1 y 100.',gap=8)
table(['Método','Ruta relativa','Resultado esperado'],[
    ('POST','/','201 Created + Location'),('GET','/','200 + página de incidencias'),
    ('GET','/{id}','200 o 404'),('PUT','/{id}','200 o 404'),('DELETE','/{id}','204 o 404'),
],[64,125,width-285])
paragraph('sensorId es una referencia del dominio. El CRUD no requiere consultar a Telemetry Service para comprobar la existencia del sensor.',SMALL)

page('2. Excepciones, pruebas y cobertura')
table(['Situación','Tratamiento y HTTP'],[
    ('Recurso inexistente','IncidentNotFoundException → 404.'),
    ('Folio duplicado','DuplicateIncidentException → 409; índice único en MongoDB.'),
    ('Entrada inválida','Validación de campos, JSON, enum y paginación → 400.'),
    ('Fallo de persistencia','DataAccessException → 503 con mensaje genérico.'),
],[147,width-243])
paragraph('El manejador global usa <b>@RestControllerAdvice</b> y devuelve JSON con la clave <b>error</b> y un mensaje descriptivo. El folio se genera en el servidor: repetir sensor o título está permitido; el conflicto 409 protege la unicidad del folio.')
section('Pruebas ejecutadas')
table(['Suite','Casos','Evidencia funcional'],[
    ('IncidentServiceTests','7','JUnit y Mockito: altas, consultas, listado, actualización, eliminación y errores.'),
    ('IncidentControllerTests','4','MockMvc: contrato HTTP, JSON, validaciones y errores de negocio/persistencia.'),
    ('IncidentMongoIT','1','Spring Boot + MongoDB 8.0 real en Testcontainers: CRUD, índice único y contrato OpenAPI.'),
],[152,55,width-303])
paragraph('Resultado: <b>12 pruebas, 0 fallos, 0 errores y 0 omitidas</b>. La integración comprueba la persistencia de las modificaciones y que consultar un recurso eliminado devuelva 404. Los contenedores temporales se retiran al finalizar.')
section('JaCoCo')
paragraph(f'El reporte registra <b>{covered} de {total} líneas cubiertas ({covered/total*100:.2f} %)</b>. Se configuraron reglas de al menos 80 % para líneas y ramas. No se detectaron ramas medibles (0 de 0): se reportan como <b>n/a</b>, no como 100 %. El 98 % visible en la columna principal del HTML corresponde a instrucciones, no a líneas.')
paragraph('El registro completo está en evidence/verify.txt. El HTML portable y XML de cobertura se conservaron en evidence/jacoco/. La ejecución terminó con BUILD SUCCESS y aprobó las reglas de cobertura.',SMALL)

page('3. SonarQube y entrega reproducible')
paragraph('Se ejecutó SonarQube Community Build 26.11.0.134175 sobre Java principal, pruebas y pom.xml, con el perfil activo Sonar way comprehensive. Se importaron el XML de JaCoCo y los reportes de pruebas unitarias y de integración.')
table(['Condición global de HU-01','Umbral','Resultado'],[
    ('Cobertura','≥ 80 %',MEASURES['coverage']+' %'),
    ('Bugs','0',MEASURES['bugs']),('Vulnerabilidades','0',MEASURES['vulnerabilities']),
    ('Code Smells','0',MEASURES['code_smells']),
],[226,110,width-432])
paragraph('<b>Quality Gate: HU-01 Incidencias, estado OK / Passed.</b> Las condiciones globales de la tabla se evaluaron y aprobaron. La configuración también conserva las condiciones estándar del servidor para código nuevo.')
paragraph('Se registraron 0 Security Hotspots y 0.0 % de duplicación. Los resultados corresponden al alcance del analizador y perfil utilizados; no constituyen un análisis de dependencias. Los tokens temporales de análisis se revocaron al finalizar.',SMALL)
section('Ejecución desde la raíz del repositorio')
paragraph('<font face="Courier" size="9">docker compose -f incident-service/compose.yaml up --build -d<br/>mvn -f incident-service/pom.xml clean verify</font>')
paragraph('Swagger: <link href="http://localhost:8083/swagger-ui/index.html" color="#007E87">http://localhost:8083/swagger-ui/index.html</link><br/>MongoDB de desarrollo: localhost:27018, base incidents. Los puertos se publican en loopback para el laboratorio.')
paragraph('Con SonarQube iniciado y un SONAR_TOKEN válido configurado externamente:<br/><font face="Courier" size="8.5">mvn -f incident-service/pom.xml sonar:sonar "-Dsonar.qualitygate.wait=true"</font><br/>SONAR_HOST_URL debe apuntar al servidor; en esta práctica se usó http://localhost:9000.',gap=9)
paragraph('Código y README: <link href="https://github.com/JustPolar1/telemetria/tree/feature/incidents-mongodb-hu01/incident-service" color="#007E87">abrir módulo en la rama de entrega</link>. Los cambios se registraron en commits de implementación, pruebas, despliegue y documentación, en una rama feature creada desde Develop.',SMALL)
paragraph('Referencias: <link href="https://docs.spring.io/spring-boot/reference/data/nosql.html" color="#007E87">Spring Boot / MongoDB</link> · <link href="https://springdoc.org/" color="#007E87">springdoc / Swagger UI</link> · <link href="https://www.jacoco.org/jacoco/trunk/doc/check-mojo.html" color="#007E87">JaCoCo check</link> · <link href="https://docs.sonarsource.com/sonarqube-community-build/user-guide/managing-tokens" color="#007E87">SonarQube</link>.',SMALL)

figures=[
    ('4. Crear incidencia - POST 201','01-post.png','Solicitud enviada desde Swagger UI. El servidor devuelve ID, folio y fecha de creación; la incidencia se guarda en MongoDB.'),
    ('5. Consultar incidencia - GET 200','02-get.png','Consulta del mismo ID creado en la página anterior. La respuesta recupera el documento persistido.'),
    ('6. Listar incidencias - GET 200','07-list.png','Listado paginado que contiene el documento creado; se muestran content y los metadatos de paginación.'),
    ('7. Actualizar incidencia - PUT 200','03-put.png','Se reemplazan los campos editables y se cambia el estado a RESUELTA, conservando ID, folio y fecha de creación.'),
    ('8. Eliminar incidencia - DELETE 204','04-delete.png','El servidor confirma la eliminación del documento sin cuerpo de respuesta.'),
    ('9. Recurso inexistente - GET 404','05-not-found.png','Después de eliminar el documento, consultar el mismo ID devuelve un error JSON descriptivo con estado 404.'),
    ('10. Validar entrada - POST 400','06-validation.png','Un cuerpo vacío es rechazado por las validaciones del modelo y el manejador global devuelve HTTP 400.'),
    ('11. Ejecución de pruebas','tests-execution.png','Captura del registro real de Maven al finalizar la integración y la comprobación de cobertura. El registro íntegro está conservado en evidence/verify.txt.'),
    ('12. Reporte HTML de JaCoCo','jacoco-report.png','Reporte generado por clean verify: 2 líneas sin cubrir de 39 (37 cubiertas, 94.87 %). Las ramas aparecen como n/a porque no hay ramas medibles.'),
]
for title,filename,caption in figures: evidence_page(title,filename,caption)

page('13. Panel de SonarQube - Quality Gate aprobado',wide=True)
paragraph('Fragmentos de la misma captura del panel global: Quality Gate Passed, cobertura 94.9 % y cero problemas abiertos de seguridad, fiabilidad y mantenibilidad. La captura íntegra se conserva en evidence/sonar-dashboard.png.',SMALL,gap=8)
image=PILImage.open(EVIDENCE/'sonar-dashboard.png')
for index, box in enumerate([(260,220,1400,495),(285,575,1395,1007)]):
    if index:
        page('14. SonarQube - métricas globales',wide=True)
        paragraph('Detalle de Overall Code en la misma captura: cero problemas abiertos, cobertura de 94.9 % y duplicación de 0.0 %. El JSON original de métricas se conserva en evidence/sonar-results.json.',SMALL,gap=14)
    fragment=image.crop(box)
    scale=min((width-96)/fragment.width,0.64)
    iw,ih=fragment.width*scale,fragment.height*scale
    if y-ih<48: raise ValueError('Sonar evidence overflows')
    pdf.drawImage(ImageReader(fragment),48+(width-96-iw)/2,y-ih,width=iw,height=ih)
    y-=ih+8
    if not index:
        table(['Condición global','Umbral exigido','Valor evaluado'],[
            ('Cobertura','Al menos 80 %','94.9 %'),('Bugs','0','0'),
            ('Vulnerabilidades','0','0'),('Code Smells','0','0'),
        ],[340,200,width-636])

pdf.save()
print(f'{OUTPUT} ({page_number} pages)')
