import { describe, expect, it } from 'vitest'
import { formatDiscount } from '@/utils/pricing'
import { h5OrderStatusText, matchesH5OrderFilter } from '@/utils/h5OrderStatus'

describe('h5 order status', () => {
  it('maps paid orders to shipping for users', () => {
    expect(h5OrderStatusText.PAID).toBe('配送中')
    expect(h5OrderStatusText.DELIVERING).toBe('配送中')
  })

  it('groups paid and delivering orders under shipping filter', () => {
    expect(matchesH5OrderFilter('PAID', 'SHIPPING')).toBe(true)
    expect(matchesH5OrderFilter('DELIVERING', 'SHIPPING')).toBe(true)
    expect(matchesH5OrderFilter('CREATED', 'SHIPPING')).toBe(false)
  })

  it('groups finished and cancelled orders under done filter', () => {
    expect(matchesH5OrderFilter('FINISHED', 'DONE')).toBe(true)
    expect(matchesH5OrderFilter('CANCELLED', 'DONE')).toBe(true)
    expect(matchesH5OrderFilter('PAID', 'DONE')).toBe(false)
  })
})

describe('book discount formatting', () => {
  it('formats whole and one-decimal discounts', () => {
    expect(formatDiscount(80, 100)).toBe('8折')
    expect(formatDiscount(85, 100)).toBe('8.5折')
  })

  it('hides discounts when original price is not higher than sale price', () => {
    expect(formatDiscount(100, 100)).toBe('')
    expect(formatDiscount(100, undefined)).toBe('')
  })
})
