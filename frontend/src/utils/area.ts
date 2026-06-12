import { areaList } from '@vant/area-data'
import type { CascaderOption } from 'vant'

type AreaOption = CascaderOption & {
  text: string
  value: string
  children?: AreaOption[]
}

function entries(record: Record<string, string>) {
  return Object.entries(record).map(([value, text]) => ({ value, text }))
}

export function buildAreaOptions(): AreaOption[] {
  const provinces = entries(areaList.province_list)
  const cities = entries(areaList.city_list)
  const counties = entries(areaList.county_list)

  return provinces.map((province) => {
    const provincePrefix = province.value.slice(0, 2)
    const cityChildren = cities
      .filter((city) => city.value.startsWith(provincePrefix))
      .map((city) => {
        const cityPrefix = city.value.slice(0, 4)
        return {
          ...city,
          children: counties.filter((county) => county.value.startsWith(cityPrefix)),
        }
      })

    return {
      ...province,
      children: cityChildren,
    }
  })
}

export const areaOptions = buildAreaOptions()

export function findProvinceCode(province?: string) {
  if (!province) return ''
  return String(areaOptions.find((option) => option.text === province)?.value || '')
}

export function findAreaCode(province?: string, city?: string, district?: string) {
  if (!province || !city || !district) return ''

  for (const provinceOption of areaOptions) {
    if (provinceOption.text !== province) continue
    for (const cityOption of provinceOption.children || []) {
      if (cityOption.text !== city) continue
      const districtOption = (cityOption.children || []).find((option) => option.text === district)
      return String(districtOption?.value || '')
    }
  }

  return ''
}
