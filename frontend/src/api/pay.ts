import type { JsapiPayParams } from './types'

export function normalizeJsapiPayPackage(params: JsapiPayParams) {
  return params.package || params.packageValue || ''
}
