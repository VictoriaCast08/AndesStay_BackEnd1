package cl.duoc.andesstay.notify.config;

/**
 * Topologia RabbitMQ del caso (6 colas: 3 flujos + 3 DLQ).
 * Se declara cuando RABBITMQ_HOST este disponible en el entorno.
 */
public final class RabbitTopology {

    public static final String EXCHANGE_CMD_DIRECT = "cmd.direct";
    public static final String EXCHANGE_CMD_TOPIC = "cmd.topic";
    public static final String EXCHANGE_DEAD = "cmd.dead.dlx";

    public static final String Q_EMAIL = "q.cmd.email";
    public static final String Q_EMAIL_DLQ = "q.cmd.email.dlq";
    public static final String Q_HOUSEKEEPING = "q.cmd.housekeeping";
    public static final String Q_HOUSEKEEPING_DLQ = "q.cmd.housekeeping.dlq";
    public static final String Q_VOUCHER = "q.cmd.voucher";
    public static final String Q_VOUCHER_DLQ = "q.cmd.voucher.dlq";

    private RabbitTopology() {}
}
