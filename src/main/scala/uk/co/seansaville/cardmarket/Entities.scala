package uk.co.seansaville.cardmarket

case class Product(
  productID: Int,
  metaProductID: Int,
  countReprints: Int,
  name: String,
  localizations: List[Localization],
  category: Category,
  website: String,
  image: String,
  gameName: String,
  categoryName: String,
  number: Int,
  rarity: String,
  expansionName: String,
  links: List[String],
  expansion: Expansion,
  priceGuide: PriceGuide,
  reprint: List[Reprint]
)

case class Expansion(
  id: Int,
  name: String,
  localizations: List[Localization],
  abbreviation: String,
  icon: Int,
  releaseDate: String,
  isReleased: Boolean,
  gameID: Int,
  links: List[String]
)

case class Category(id: Int, name: String)

case class Reprint(id: Int, expansion: String, icon: Int)

case class Localization(name: String, languageID: Int, languageName: String)

case class PriceGuide(
  sell: Float,
  low: Float,
  lowEx: Float,
  lowFoil: Float,
  average: Float,
  trend: Float
)
