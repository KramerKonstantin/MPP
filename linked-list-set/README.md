# Множество на основе односвязного списка

Проект включает в себя следующие исходные файлы:
* `Set.java` содержит интерфейс множества.
* `SetImpl.java` содержит реализацию множества на основе односвязного списка для однопоточного случая. Данная реализация небезопасна для использования из нескольких потоков одновременно.

Необходимо доработать реализую `SetImpl` так, чтобы она стала безопасной для использования из множества потоков одновременно.
Используйте неблокирующую синхронизацию для всех операций.
Вместо `AtomicMarkableReference` предлагается в поле `next` хранить либо `Node` (когда флаг `removed == false`), либо специально добавленный `class Removed { final Node next; }` (когда `removed == true`).
Таким образом, чтобы проверить, выставлен ли флаг `removed`, необходимо написать `next instanceof Removed`.
Посмотрите на реализацию `AtomicMarkableReference`, она примерно так и сделана.

## Сборка и тестирование
Для тестирования используйте:

* `FunctionalTest.java` проверяет базовую корректность множества.
* `LinearizabilityTest.java` проверяет реализацию множества на корректность в многопоточной среде.

Обратите внимание, что тесты не покрывают все возможные ошибки синхронизации, поэтому прохождение тестов не означает корректность реализации.

## Ограничения
* Все атомарные операции должны выполняться при помощи примитивов из библиотеки `kotlinx.atomicfu`.
* Использования любых примитивов из пакета `java.util.concurrent.*`,  `synchronized` методов и блоков запрещены.
* Разрешается редактирование только файла `SetImpl.java`.