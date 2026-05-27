package com.divyam.advent.service;

import java.util.Map;
import java.util.Optional;

/**
 * Russian translations for the PDF-driven challenge cycle, keyed by {@code cycleDay}.
 * Applied to cycle challenges by {@link ChallengeCycleSyncService} (English stays as the
 * fallback when a day has no entry here). Keyed by day number so it matches regardless of
 * how the source PDF parses each day's title.
 */
public final class ChallengeCycleRuTranslations {

    public record RuText(String title, String description) {
    }

    private static final Map<Integer, RuText> BY_DAY = Map.ofEntries(
            Map.entry(1, new RuText("Уборка-перезагрузка", "Сделай уборку.")),
            Map.entry(2, new RuText("Цветущее дерево", "Найди ближайшее цветущее дерево (например, яблоню или вишню на юге, или просто первые листья на севере) и сфотографируй.")),
            Map.entry(3, new RuText("Момент в небе Хамамацу", "Фестиваль Хамамацу. Запусти бумажный самолётик в небо.")),
            Map.entry(4, new RuText("Размышление о свободе прессы", "Всемирный день свободы печати. Сделай зин о том, что тебе важно обсудить.")),
            Map.entry(5, new RuText("Снимок еды", "Сфотографируй свою еду.")),
            Map.entry(6, new RuText("Цветная прогулка", "Цветная прогулка. Фотографируй один определённый цвет во время прогулки. Выбери самый удачный кадр и загрузи его.")),
            Map.entry(7, new RuText("Открытка от души", "Сделай и подари кому-нибудь простую открытку.")),
            Map.entry(8, new RuText("Кадр как из кино", "Сделай фото в стиле момента из фильма.")),
            Map.entry(9, new RuText("Остановка у статуи", "Найди статую.")),
            Map.entry(10, new RuText("Местный снек", "Попробуй местный снек.")),
            Map.entry(11, new RuText("Каллиграфия", "Попробуй красиво написать в тетради алфавит языка, который учишь, как каллиграфию. Загрузи фото.")),
            Map.entry(12, new RuText("Лимерик", "День лимерика. Сочини свой лимерик.")),
            Map.entry(13, new RuText("Местный рынок", "Исследуй местный рынок.")),
            Map.entry(14, new RuText("История города", "Поговори с местным продавцом или гидом и узнай одну интересную историю из истории твоего города.")),
            Map.entry(15, new RuText("Звонок семье", "Международный день семьи. Свяжись со своей семьёй.")),
            Map.entry(16, new RuText("Цветочная луна", "Цветочная луна. Собери небольшой букет во время прогулки.")),
            Map.entry(17, new RuText("Тайное место заката", "Посмотри закат из нетуристического места.")),
            Map.entry(18, new RuText("День музея", "День музея. Сходи в любой доступный музей.")),
            Map.entry(19, new RuText("День воды", "Выпей сегодня два литра воды.")),
            Map.entry(20, new RuText("Универ в кадре", "Сделай креативное фото, обрамив свой университет через что-нибудь.")),
            Map.entry(21, new RuText("День культуры", "Всемирный день культурного разнообразия ЮНЕСКО. Добавь в свой образ элемент своей культуры. Узнай, какая национальная одежда есть у других стран.")),
            Map.entry(22, new RuText("Найти общий язык", "Найди общий язык. Настольные игры. Сыграй в Крокодила.")),
            Map.entry(23, new RuText("Новая улица", "Пройдись по улице, которую ещё не исследовал.")),
            Map.entry(24, new RuText("Музыка мира", "Послушай альбом, популярный в другой стране.")),
            Map.entry(25, new RuText("День Африки", "День Африки. Выучи столицы крупнейших стран Африки.")),
            Map.entry(26, new RuText("Вечер Дракулы", "День Дракулы. Время кино. Посмотри фильм про вампиров.")),
            Map.entry(27, new RuText("Неочевидный комплимент", "Сделай кому-нибудь комплимент за что-то неочевидное.")),
            Map.entry(28, new RuText("Фотоохота за птицами", "Найди три разных вида птиц и сфотографируй их.")),
            Map.entry(29, new RuText("Дудл", "Нарисуй дудл и сфотографируй его.")),
            Map.entry(30, new RuText("День добрых соседей", "Европейский день соседей. Если живёшь в общежитии, оставь соседу записку или конфету с приятным сообщением. Иначе сделай то же для соседей по парте в университете."))
    );

    private ChallengeCycleRuTranslations() {
    }

    public static Optional<RuText> forDay(Integer cycleDay) {
        if (cycleDay == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_DAY.get(cycleDay));
    }
}
