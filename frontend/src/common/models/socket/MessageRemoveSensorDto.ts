import { Transform } from 'class-transformer';
import { transformerDateTime } from 'src/common/models/models';
import dayjs from 'dayjs';
import { SocketEvents } from 'boot/socket';

export class RemoveSensorDto {
  sensor?: string;
}

export class MessageRemoveSensorDto {
  @Transform(transformerDateTime)
  date?: dayjs.Dayjs | undefined;

  type?: SocketEvents | undefined;

  declare content: RemoveSensorDto;
}
