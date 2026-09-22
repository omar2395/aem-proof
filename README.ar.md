# AEM Proof

[![CI](https://github.com/omar2395/aem-proof/actions/workflows/ci.yml/badge.svg)](https://github.com/omar2395/aem-proof/actions/workflows/ci.yml)

مشروع Adobe Experience Manager (AEM as a Cloud Service) صغير ومكتمل. الهدف منه أن يعرض، بكود يمكنك استنساخه وبناؤه، كيف أبني مكوّنات AEM وSling Models وخدمات OSGi والتكاملات الخارجية.

**English:** [README.md](README.md)

## ما الذي يوجد هنا

- **مكوّن مخصص بحوار تحرير وHTL وSling Model:** `ui.apps/…/components/offers-grid` و`core/…/models/impl/OffersGridImpl.java`
- **خدمة OSGi للتكامل** بإعدادات ومهلة زمنية وذاكرة مؤقتة وقاطع دائرة (circuit breaker): `core/…/integration/`
- **نموذج Content Fragment** مع خمسة أجزاء محتوى عربية: `ui.content/…/conf/aemproof/settings/dam/cfm/models/offer`
- **رأس الصفحة كـ Experience Fragment** مثبّت داخل قالب قابل للتحرير: `content/experience-fragments/aemproof/sa/ar/site/header` و`conf/aemproof/settings/wcm/templates/page-content`
- **إعدادات OSGi حسب بيئة التشغيل** (dev / prod / author / publish): `ui.config/…/osgiconfig/`
- **قواعد Dispatcher** للتصفية والتخزين المؤقت: `dispatcher/src/conf.dispatcher.d/`
- **اختبارات وحدة** بـ AEM Mocks وWireMock مع حد أدنى للتغطية 80٪: `core/src/test/`
- **واجهة أمامية** هي مكتبة العميل الحقيقية التي يقدّمها AEM، مع معاينة تصميم ثابتة: `ui.frontend/`

## البناء

يتطلب JDK 17 وMaven 3.9. لا يلزم حساب Adobe للبناء وتشغيل الاختبارات.

```bash
mvn clean verify
```

## النشر على نسخة AEM

```bash
mvn clean install -PautoInstallSinglePackage            # author على المنفذ 4502
mvn clean install -PautoInstallSinglePackagePublish     # publish على المنفذ 4503
```

أو ادفع المستودع إلى Git الخاص بـ Cloud Manager؛ المشروع يتبع بنية Cloud Service من البداية.

## الاختبارات

`mvn -pl core verify` يشغّل اختبارات Sling Models (بـ AEM Mocks) واختبارات التكامل (بـ WireMock)، ثم يُفشل البناء إذا نزلت تغطية الأسطر عن 80٪. تُدقَّق سكربتات HTL أثناء البناء بـ `htl-maven-plugin`، ويفحص محلّل AEM Cloud Service الحزمة النهائية. التفاصيل في [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md#testing-strategy).

## معاينة التصميم

الواجهة الأمامية مكتبة عميل حقيقية مع صفحة معاينة ثابتة، فيمكن رؤية التصميم دون نسخة AEM: [docs/PREVIEW.md](docs/PREVIEW.md).

## Cloud Service أم 6.5

المشروع مولَّد لـ AEM as a Cloud Service؛ التغييرات الوحيدة اللازمة لإصدار 6.5 هي اعتمادية الـ API وعناصر الأسرار، وهي مذكورة في [ADR-0001](docs/adr/0001-cloud-service-target.md).

## التوثيق

- [المعمارية](docs/ARCHITECTURE.md)
- [سجلات القرارات](docs/adr/)
- [المساهمة والإعداد المحلي](docs/CONTRIBUTING.md)
- [معاينة التصميم الثابتة](docs/PREVIEW.md)
- [سجل التغييرات](CHANGELOG.md)

## ملاحظة حول التشغيل

لا يمكن تشغيل AEM دون مؤسسة Adobe مرخّصة، لذلك يقدّم هذا المستودع كوداً واختبارات لا رابط AEM عاماً. أضِفني إلى Adobe Admin Console لديكم ويكون المشروع منشوراً على بيئة التطوير خلال ساعة.
