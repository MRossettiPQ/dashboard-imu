import type { SessionSensorDto } from 'src/common/models/socket/SessionSensorDto';
import { Transform } from 'class-transformer';
import { transformerDateTime } from 'src/common/models/models';
import dayjs from 'dayjs';
import { SocketEvents } from 'src/common/models/socket/SocketEvents';

export class MessageSensorListDto {
  @Transform(transformerDateTime)
  date?: dayjs.Dayjs | undefined;

  type?: SocketEvents | undefined;

  content: SessionSensorDto[] = [];

  originIdentifier?: string | undefined;
}
