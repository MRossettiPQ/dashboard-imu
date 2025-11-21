import { Transform } from 'class-transformer';
import { transformerDateTime } from 'src/common/models/models';
import dayjs from 'dayjs';
import { SocketEvents } from 'boot/socket';
import { Measurement } from 'src/common/models/measurement/Measurement';

export class MessageClientMeasurementBlock {
  @Transform(transformerDateTime)
  date?: dayjs.Dayjs | undefined;

  type?: SocketEvents | undefined = SocketEvents.SERVER_CLIENT_MEASUREMENT;

  declare content: Measurement[];
}
