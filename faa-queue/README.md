# FAA-Based Lock-Free Queue
Необходимо доработать реализую `FAAQueue` так, чтобы она стала безопасной для использования из множества потоков одновременно.
Смотрите соответствующую лекцию.

## Тестирование
Для тестирования используйте:

* `FunctionalTest.java` проверяет базовую корректность множества.
* `LinearizabilityTest.java` проверяет реализацию множества на корректность в многопоточной среде.

Обратите внимание, что тесты не покрывают все возможные ошибки синхронизации, поэтому прохождение тестов не означает корректность реализации.

## Ограничения
* Все атомарные операции должны выполняться при помощи примитивов из библиотеки `kotlinx.atomicfu`.
* Использования любых примитивов из пакета `java.util.concurrent.*`,  `synchronized` методов и блоков запрещены.
* Разрешается редактирование только файла `MSQueue.java`.