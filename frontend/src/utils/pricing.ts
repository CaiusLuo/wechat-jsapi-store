export interface PriceLike {
  price?: number | string | null
  originalPrice?: number | string | null
}

function toPositiveNumber(value?: number | string | null) {
  const amount = Number(value ?? 0)
  return Number.isFinite(amount) && amount > 0 ? amount : 0
}

export function displayOriginalPrice(book: PriceLike) {
  const originalPrice = toPositiveNumber(book.originalPrice)
  return originalPrice > 0 ? originalPrice : toPositiveNumber(book.price)
}

export function formatDiscount(price?: number | string | null, originalPrice?: number | string | null) {
  const salePrice = toPositiveNumber(price)
  const original = toPositiveNumber(originalPrice)
  if (salePrice <= 0 || original <= 0 || salePrice >= original) {
    return ''
  }

  const discount = Math.round((salePrice / original) * 100) / 10
  const text = Number.isInteger(discount) ? String(discount) : discount.toFixed(1)
  return `${text}折`
}
