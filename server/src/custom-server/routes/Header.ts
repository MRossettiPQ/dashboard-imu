import settings from '../settings'
import { CustomNextFunction, CustomRequest, CustomResponse } from '../core/utils/ExpressUtil'

export default function Header(
  _req: CustomRequest,
  res: CustomResponse,
  next: CustomNextFunction
): void {
  const { origin, methods, allowedHeaders } = settings.host.cors
  res.header('Access-Control-Allow-Origin', origin)
  res.header('Access-Control-Allow-Methods', methods.join(','))
  res.header('Access-Control-Allow-Headers', allowedHeaders.join(','))
  next()
}
