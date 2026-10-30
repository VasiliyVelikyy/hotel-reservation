
# Основной поток разработки
```mermaid
flowchart
    DEV["Разработчик"] --->|"Изменение кода"| GIT["Git-репозиторий"]
    GIT --->|"Коммит/событие"| CI["Система CI"]
    CI --->|"Сборка и проверка "| BUILD["Собранное приложение"]
    BUILD --->|"Публикация версии"| REG["Хранилище артефактов"]
    REG --->|"Получение версии"| EVN["Среда запуска"]
    EVN ---> APP["Рест сервис бронирования"]

    classDef source fill:#e8f0fe,stroke:#4776b4,color:#17365d
    classDef process fill:#fff2cc,stroke:#bf9000,color:#594300
    classDef artifact fill:#e2f0d9,stroke:#548235,color:#274e13
    classDef runtime fill:#fce4d6,stroke:#c65911,color:#783f04
    class DEV,GIT source
    class CI,BUILD process
    class REG artifact
    class ENV,APP runtime

```


# Потоки данных и артефакты

```mermaid
flowchart
    DEV["Разработчик"]
    GIT["Git-репозиторий"]
    CI["Система CI сборка и тесты"]
    ART["Артефакт приложения"]
    REG["Хранилище артефактов"]
    CFG["Конфигурация развертывания"]
    CD["Этап доставки/развертывания"]
    EVN["Среда запуска"]

    DEV --->|"Изменение кода"| GIT
    GIT --->|"Исходный код и версия"| CI
    CI --->|"Отчет о теста"| REPORT["Результат проверки"]
    CI --->|"Проверенный артефакт"| ART
    ART --->|"Публикация версии"| REG
    REG --->|"Образ или пакет артефактов по версии"| CD
    CFG --->|"Параметры запуска"| CD
    CD --->|"Развертывание"|DEPLOY

    classDef source fill:#e8f0fe,stroke:#4776b4,color:#17365d
    classDef process fill:#fff2cc,stroke:#bf9000,color:#594300
    classDef artifact fill:#e2f0d9,stroke:#548235,color:#274e13
    classDef config fill:#fce4d6,stroke:#c65911,color:#783f04
    class GIT,DEPLOY source
    class CI,CD process
    class ART,REG,REPORT artifact
    class CFG,ENV config
```


# Версионирование и условия выпуска


```mermaid
flowchart 
    A["Изменение в git"] ---> B["Сборка"]
    B ---> C{"Сборка успешна?"}
    C -- Нет --> F["Зафиксировать ошибку"]
    F ---> G["Исправить код или конфигурацию"]
    G ---> A
    C -- Да -->D["Автоматические тесты"]
    D --> E{"Тесты пройдены?"}
    E -- Нет --> H["Сохранить отчет об ошибках"]
    H --> G
    E -- Да --> I["Создание версионированного артефакта"]
    I --> J["Опубликовать в хранилище"]
    J --> K{"Развертывание разрешено?"}
    K -- Нет --> L["Ожидание согласования"]
    K -- Да --> M["Развернуть на целевой среде"]

    classDef decision fill:#fff2cc,stroke:#bf9000,color:#594300
    classDef error fill:#f4cccc,stroke:#cc0000,color:#660000
    classDef success fill:#d9ead3,stroke:#38761d,color:#274e13
    class C,E,K decision
    class F,G,H error
    class I,J,M success
```


# Диаграмма наблюдаемости и обратной связи

```mermaid
flowchart
   GIT["Git-репозиторий"] --> CI["Система CI сборка и тесты"]
   CI -->  REG["Хранилище артефактов"]
   REG --> DEPLOY["Развертывание"]
   DEPLOY --> APP["Рест сервис бронирования"]
   
   APP --> LOGS["Логи"]
   APP --> METRIC["Метрики"]
   APP --> TRACES["Трассировка"]
   
   LOGS --> OBS["Система наблюдаемости"]
   METRIC --> OBS
   TRACES --> OBS
   
   OBS --> ALERT["Обнаружение отклонения"]
   ALERT -->ISSUE["Задача на расследование"]
   ISSUE -->GIT

    classDef process fill:#fff2cc,stroke:#bf9000,color:#594300
    classDef runtime fill:#fce4d6,stroke:#c65911,color:#783f04
    classDef observe fill:#d9ead3,stroke:#38761d,color:#274e13
    class CI,DEPLOY process
    class APP runtime
    class LOGS,METRICS,TRACES,OBS,ALERT observe
```


# Диаграмма разделения по этапам разработки и сопровождения

```mermaid

flowchart TB
  subgraph DEV["1. Разработка"]
    DEV1["Разработчик"]
    GIT["Git-репозиторий"]
    DEV1 -->|"Изменение кода"| GIT
  end

  subgraph DELIVERY["2. Сборка и доставка"]
    CI["CI: сборка и тесты"]
    CHECK{"Проверки пройдены?"}
    REG["Хранилище артефактов"]
    DEPLOY["Развёртывание"]
    GIT -->|"Версия исходного кода"| CI
    CI --> CHECK
    CHECK -->|"Нет"| FAIL["Отчёт об ошибке"]
    FAIL --> DEV1
    CHECK -->|"Да"| REG
    REG -->|"Конкретная версия"| DEPLOY
  end

  subgraph RUN["3. Эксплуатация"]
    APP["REST-сервис бронирования"]
    DB[("База данных")]
    APP --> DB
    DEPLOY --> APP
  end

  subgraph OBS["4. Наблюдаемость"]
    LOG["Логи"]
    MET["Метрики"]
    TRACE["Трассировки"]
    ALERT["Анализ отклонений"]
    APP --> LOG
    APP --> MET
    APP --> TRACE
    LOG --> ALERT
    MET --> ALERT
    TRACE --> ALERT
  end

  ALERT -->|"Инцидент / задача"| DEV1

  classDef dev fill:#e8f0fe,stroke:#4776b4,color:#17365d
  classDef delivery fill:#fff2cc,stroke:#bf9000,color:#594300
  classDef runtime fill:#d9ead3,stroke:#38761d,color:#274e13
  classDef monitoring fill:#fce4d6,stroke:#c65911,color:#783f04
  class DEV1,GIT dev
  class CI,CHECK,REG,DEPLOY,FAIL delivery
  class APP,DB runtime
  class LOG,MET,TRACE,ALERT monitoring
```